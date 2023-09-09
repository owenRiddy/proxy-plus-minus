(ns proxy-plus-minus.core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [proxy-plus-minus.core :refer [proxy+- proxy-super+-]])
  (:import
   [testclasses TestBaseClass TestBaseClass2 TestBaseClass3 InterfaceA
    InterfaceB InterfaceC AbstractBaseClass2]))

(deftest nothing-test
  (let [o (proxy+- [])
        o2 (proxy+- [] Object)]
    (is (instance? Object o))
    (is (not= (class o) Object))
    (is (instance? Object o2))
    (is (not= (class o2) Object))))

(deftest base-class-test
  (let [o
        (proxy+- ["a"]
                 TestBaseClass
                 (foo [_this s] (count s))
                 (doSomething [_this c l s d]
                              (str c l s d)))]
    (is (instance? TestBaseClass o))
    (is (= "a 11" (.getVal o)))
    (is (= "a1024.5" (.doSomething o \a 10 2 4.5)))
    ;; NOTE generates a reflection warning because this isn't a real field / fn!
    (is (thrown? Exception (.foo2 o)))
    (is (= 3 (.foo o "aaa")))
    (is (= 5 (.foo o "edcba")))))

(deftest multiple-levels-of-base-classes-test
  (let [_o
        (proxy+-
         []
         AbstractBaseClass2
         (foo [_this ^Integer _x ^String _y])
         (bar [_this ^Integer _x ^Integer _y ^long _z]))]))

(definterface I1
  (^String foo [^Long l ^long l2])
  (^void bar [^Object o]))

(definterface I2
  (^Object foo [])
  (^void car []))

(definterface I3
  (^String foo [^Long l ^long l2]))

(deftest only-interfaces-test
  (let [v
        (volatile! 0)

        o
        (proxy+- []
                 I1
                 (foo [_this l l2] (str (+ l l2)))
                 (bar [_this _o] (vswap! v inc))
                 I2
                 (foo [_this] 99)
                 (car [_this] (vswap! v #(* 10 %))))]
    (is (instance? I1 o))
    (is (instance? I2 o))
    (is (= "13" (.foo o 10 3)))
    (is (= 0 @v))
    (.bar o nil)
    (is (= 1 @v))
    (is (= 99 (.foo o)))
    (.car o)
    (is (= 10 @v))))

(deftest superclass-and-interface-test
  (let [v
        (volatile! 0)

        o
        (proxy+- []
                 TestBaseClass
                 (foo2 [_this] (vswap! v inc))
                 I1
                 (bar [_this o] (vreset! v o) (str o "!")))]
    (is (instance? TestBaseClass o))
    (is (instance? I1 o))
    (is (not (instance? I2 o)))
    (is (= 0 @v))
    (.foo2 o)
    (is (= 1 @v))
    (is (nil? (.bar o "a")))
    (is (= "a" @v))))

(deftest super-args-test
  (let [o (proxy+- []
                   TestBaseClass)
        o2 (proxy+- ["bbb"]
                    TestBaseClass)
        o3 (proxy+- ["z" -1]
                    TestBaseClass)]
    (is (= "s 10" (.getVal o)))
    (is (= "bbb 11" (.getVal o2)))
    (is (= "z -1" (.getVal o3)))))

(deftest overlapping-interfaces-test
  (let [o
        (proxy+- []
                 I1
                 (foo [_this l1 l2] (str (- l1 l2)))
                 I3)

        o2
        ((fn [x] x) o)]
    (is (instance? I1 o))
    (is (instance? I3 o))
    (is (= "-7" (.foo ^I1 o2 3 10)))
    (is (= "-7" (.foo ^I3 o2 3 10)))))

(definterface I4
  (^Object foo [])
  (^Object foo [arg]))

(deftest multiple-arity-same-method-name-test
  (let [o
        (proxy+- []
                 I4
                 (foo [_this] :a)
                 (foo [_this arg] (str arg "?")))]
    (is (= :a (.foo o)))
    (is (= "hello?" (.foo o "hello")))))

(definterface I5
  (^String foo [^String str])
  (^String foo [^Integer n]))

(deftest same-method-name-different-param-types
  (let [o1
        (proxy+- []
                 I5
                 (foo [_this ^String _strArg] "other")
                 (foo [_this ^Integer _integerArg] "first"))

        sb
        (new StringBuilder)

        o2
        (proxy+- []
                 java.io.Writer
                 (write [_this ^String _str _offset _len]
                        (.append sb "String overload")
                        nil)
                 (write [_this ^chars _cbuf _offset _len]
                        (.append sb "char[] overload")
                        nil))

        o3
        (proxy+- []
                 InterfaceA
                 (bar [_this ^Integer _x ^Integer _y ^long _z] "barA")

                 InterfaceB
                 (bar [_this ^Integer _x ^Integer _y ^Integer _z] "barB")

                 InterfaceC
                 (baz [_this] 0)
                 (baz [_this ^long _p] 1)
                 (baz [_this ^double _p] 2)
                 (baz [_this ^ints _p] 3)
                 (baz [_this ^chars _p] 4))]
    (is (= "other" (.foo ^I5 o1 "bar")))
    (is (= "first" (.foo ^I5 o1 ^Integer (int 3))))
      ;; overloads of concrete Java Writer class
    (is (= nil (.write o2 "hello" 0 5)))
    (is (= "String overload" (.toString sb)))
    (.setLength sb 0)
    (is (= nil (.write o2 (char-array [\f \o \o]) 1 1)))
    (is (= "char[] overload" (.toString sb)))
      ;; same method and arity, but from two different super interfaces
      ; from InterfaceA
    (is (= "barA" (.bar ^InterfaceA o3 (int 1) (int 1) (long 2))))
      ; from InterfaceB
    (is (= "barB" (.bar ^InterfaceB o3 (int 1) (int 1) (int 2))))
    (is (= 0 (.baz o3))) ; 0-arity overload
    (is (= 1 (.baz o3 (long 1)))) ; 1-arity (long) overload
    (is (= 2 (.baz o3 (double 1.0)))) ; 1-arity (double) overload
    (is (= 3 (.baz o3 (int-array [1 2 3])))) ; 1-arity (int[]) overload
    (is (= 4 (.baz o3 (char-array "zyx")))) ; 1-arity (char[]) overload
    ))

(deftest inherited-test
  (let [o
        (proxy+- []
                 TestBaseClass2
                 (foo [_this arg] (* 2 (count arg)))
                 (car [_this] "inherited"))]
    (is (= 12 (.foo o "biubiu")))
    (is (= "inherited" (.car o)))))

(deftest named-proxy-test
  (let [o
        (proxy+- my-proxy [])]
    (is (= (.getName (class o))
           "proxy_plus_minus.core_test.my_proxy"))))

(definterface I6
  (^String foo [^java.util.Map m])
  (^String foo [^java.util.HashMap m]));

(deftest assignable-from-test
  (let [o
        (proxy+-
         []
         I6
         (foo [_this ^java.util.HashMap _m] "woo")
         (foo [_this ^java.util.Map _m] "wee"))]

    (is (= "wee" (.foo o (java.util.TreeMap.))))
    (is (= "woo" (.foo o (java.util.HashMap.))))))

(deftest throws-on-busted-type-hint-test
  (is (thrown? Exception
               ;; eval here so that the test namespace as a whole compiles, even
               ;; though this produces a compile-time error!
               (eval '(proxy+
                       []
                       I6
                       (foo [_this ^SuperDuperMap m] "woo"))))))

(deftest proxy-super+--test

  (testing "Check TestBaseClass3"
    (let [o
          (proxy+- [] TestBaseClass3)]
      (is (= 100 (.getInt o)))
      (is (= 0.0 (.getDouble o 1 2.0 "three" (.intValue 4) false)))
      (is (= 7.0 (.getDouble o 1 2.0 "three" (.intValue 4) true)))
      (is (= "Yes" (.getString o "Yes")))))

  (testing "Check TestBaseClass3 with overrides"
    (let [o
          (proxy+- [] TestBaseClass3
                   (getInt [_this] (.intValue 3))
                   (getDouble [_this _a _b _c _d _e] 4.0)
                   (getString [_this _a] "No"))]
      (is (= 200 (.getOtherInt o)))
      (is (= 3 (.getInt o)))
      (is (= 4.0 (.getDouble o 1 2.0 "three" (.intValue 4) true)))
      (is (= "No" (.getString o "Yes")))))

  (testing "Check proxy-super+"
    (let [o
          (proxy+- [] TestBaseClass3
                   (getInt [_this] (.intValue 3))
                   (getDouble [_this _a _b _c _d _e] 4.0)
                   (getString [_this _a] "No"))]
      (is (= 3 (.getInt o)))
      (is (= 100 (proxy-super+- getInt o)))
      (is (= 4.0 (.getDouble o 1 2.0 "three" (.intValue 4) true)))
      (is (= 0.0 (proxy-super+- getDouble o 1 2.0 "three" (.intValue 4) false)))
      (is (= 7.0 (proxy-super+- getDouble o 1 2.0 "three" (.intValue 4) true)))
      (is (= "No" (.getString o "Yes")))
      (is (= "Yes" (proxy-super+- getString o "Yes"))))))

(deftest proxy+-hard-signatures-test
  (testing "Check a tricky edge case"
    (let [o
          (proxy+- [] TestBaseClass3
                  ;; Impossible: "Only long and double primitives are supported"
                   #_(trickyCase [this ^int a b] 8)
                  ;; hard to test, but this is a demo of what an illegal method override does
                   #_(trickyCase [this a b] [char java.lang.String :=> int] 8)
                   (trickyCase [_this _a _b] [int java.lang.String :=> int] 8)
                   (trickyCase [_this _a _b] [java.lang.Integer java.lang.String :=> int] 9))]
      (is (= 8 (.trickyCase o ^int (.intValue 1) "Two")))
      (is (= 9 (.trickyCase o ^java.lang.Integer (.intValue 1) "Two"))))))