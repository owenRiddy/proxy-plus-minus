# proxy-plus-minus

Nathan Marz built a great Proxy library called proxy+. This project is first and foremost a copy of that work. I'm aiming to make 2x major changes:

1. Rewrite it in my own code style. This is where the `minus` in the name is a factor - I'm pretty sure this will slow the code down. I want to despecialise the dependencies too.
2. Support a (proxy-super+ ...) function/macro. Vanilla Clojure has a [crippling bug in proxy-super](https://clojure.atlassian.net/browse/CLJ-2201) and there is an opportunity here to work around it.

`proxy+` is a replacement for Clojure's `proxy` that's faster and more usable. `proxy` has a strange implementation where it overrides every possible method and uses a mutable field to store a map of string -> function for dispatching the methods. This causes it to be unable to handle methods with the same name but different arities.

`proxy+` fixes these issues with `proxy`. Usage is like `reify`, and it's up to 10x faster.

## Usage

This library provides the macro `proxy+`. The first argument is fields to provide to the superclass's constructor. Next comes `reify`-like definitions to provide overrides.  When extending a base class, the base class should come first. Example usage:

```clj
(proxy+ [super-arg1 super-arg2]
  BaseClass
  (foo [this] -1)
  (foo [this a b] (+ a b))

  SomeInterface
  (bar [this a b c] (* a b c))

  SomeInterface2
  (bar [this] 100)
  )
```

## License

Copyright 2020 Red Planet Labs, Inc. proxy-plus is licensed under Apache License v2.0.
