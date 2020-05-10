# QuickCLJS

A super-quick way to bootstrap a CLJS Reagent project.

Provide a top-level app-view to QuickCLJS and it takes care of the rest (figwheel config, index.html, reloading, etc.)

## Get Started

1. Add the QuickCLJS dependency to your `project.clj`:
```clojure
[io.bloomventures/quickcljs "0.0.1"]
```

2. Add some config to your `project.clj` to point to a function (Reagent component) that will be your top-level view:
```clojure
:quickcljs-view myapp.some.ns/app-view
```

3. Launch a Leiningen REPL:
```clojure
lein repl
```

4. Require QuickCLJS:
```clojure
(require 'quickcljs.core)
```

5. Start Figwheel:
```clojure
(quickcljs.core/start!)
```

6. Navigate to the printed server URL (a random available port is chosen the first time you run)

7. Enjoy!

8. To stop Figwheel:
```clojure
(quickcljs.core/stop!)
```

