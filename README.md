# proxy-plus-minus

Nathan Marz built a great Proxy library called [proxy-plus](https://github.com/redplanetlabs/proxy-plus). This project is first and foremost a copy of that work. I'm aiming to make a few significant changes:

1. Lint & push code towards my preferred style. This is where the `minus` in the name is a factor.
2. Despecialise the dependencies: generic https://asm.ow2.io/ ASM libraries so that the documentation is easier to look up (although RPL's Rama project does sound interesting).
3. Support a (proxy-super+ ...) function/macro. Vanilla Clojure has a [crippling bug in proxy-super](https://clojure.atlassian.net/browse/CLJ-2201) and there is an opportunity here to work around it.

`proxy+-` is a replacement for Clojure's `proxy` that's faster and more usable. `proxy` has a strange implementation where it overrides every possible method and uses a mutable field to store a map of string -> function for dispatching the methods. This causes it to be unable to handle methods with the same name but different arities.

`proxy+-` fixes these issues with `proxy`. Usage is like `reify`, and it's up to 10x faster.

## Usage

This library provides the macro `proxy+-`. The first argument is fields to provide to the superclass's constructor. Next comes `reify`-like definitions to provide overrides.  When extending a base class, the base class should come first. Example usage:

```clj
(ns example
  (:require [proxy-plus-minus.core :refer [proxy+ proxy-super+]]))

(proxy+- [super-arg1 super-arg2]
  BaseClass
  (foo [this] -1)
  (foo [this a b] (+ a b))

  SomeInterface
  (bar [this a b c] (* a b c))

  SomeInterface2
  (bar [this] 100))

;; TestBaseClass3 has a .getInt method that returns 100
(let [o
      (proxy+- []
        TestBaseClass3
        (getInt [this] (.intValue 3)))]
  (is (= 100 (proxy-super+- getInt o))))
```

## Naming

> Isn't `proxy+-` a terrible name?
~ Me

Yes. This project contains several hacks and we all wish that clojure.core would fix up the `proxy-super` macro so that this wasn't necessary, or think of a way for to provide compiler support for the proxy-plus project. I've had trivial documentation PRs sit waiting for months to get in to Clojure website and I'd like a working `proxy-super` in September so this hack is the next best thing. But the name is a passive reminder that if any alternative library suits your use case then that is the preferred option.

## License

Copyright 2020 Red Planet Labs, Inc. proxy-plus-minus is licensed under Apache License v2.0.
