# QuickCLJS

A super-quick way to bootstrap a CLJS Reagent project.

Provide a top-level app-view to QuickCLJS and it takes care of the rest (figwheel config, index.html, reloading, etc.)

## Get Started

1. Create a `project.clj` with the QuickCLJS dependency, and a reference to your top-level function (reagent component):
```clojure
(defproject myapp "0.0.1"
  :dependencies [[org.clojars.rafd/quickcljs "0.0.2"]]
  :quickcljs-view myapp.core/app-view)
```

2. Create that namespace and view (ex. `src/myapp/core.cljs`)

```clojure
(ns myapp.core)

(defn app-view []
  "Hello World")
```

3. Launch a Leiningen REPL:
```clojure
lein repl
```

4. Require QuickCLJS:
```clojure
(require 'quickcljs.go)
```

6. Navigate to the printed server URL (a random available port is chosen the first time you run)

7. Enjoy!

8. To stop or restart the build:
```clojure
(quickcljs.go/stop!)
(quickcljs.go/start!)
```

