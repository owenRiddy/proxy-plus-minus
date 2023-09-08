(defproject proxy-plus-minus "0.0.12-SNAPSHOT"
  :description "A faster and more usable replacement for Clojure's proxy."
  :test-paths ["test/clj"]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.ow2.asm/asm "4.2"]
                 [org.ow2.asm/asm-analysis "4.2"]
                 [org.ow2.asm/asm-commons "4.2"]
                 [org.ow2.asm/asm-util "4.2"]]

  :profiles {:dev {:java-source-paths ["test/java"]}
             :bench {:dependencies [[criterium "0.4.5"]]}})
