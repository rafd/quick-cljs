(ns quickcljs.core
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojure.core.server :as server]
    [cljs.repl]
    [hiccup.core :as hiccup]
    [figwheel.main.api :as repl-api])
  (:import
    (java.net ServerSocket)))

(defn io-repl
  [& {:keys [repl-env]}]
  (cljs.repl/repl repl-env))

(defn next-available-port
  "Returns next available port #"
  []
  (let [s (ServerSocket. 0)
        port (.getLocalPort s)]
    (.setReuseAddress s true)
    (.close s)
    port))

(defn ring-handler [request]
  (cond
    (= (request :uri) "/")
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body
     (hiccup/html
       [:html
        [:head
         [:title "QuickCLJS"]]
        [:body
         [:div#app]
         [:script {:type "text/javascript" :src "/js/app.js"}]
         [:script {:type "text/javascript"} "quickcljs.core.init();"]]])}

    (= (request :uri) "/favicon.ico")
    {:status 200
     :body nil}

    (string/starts-with? (request :uri) "/js/")
    {:status 200
     :headers {"Content-Type" "application/javascript; charset=utf-8"
               "Cache-Control" "no-store"}
     :body (slurp (-> (request :uri)
                      (string/replace-first "/js/" "./target/quickcljs-output/")))}

    :else
    {:status 404}))

(defn config-app-view-symbol []
  (->> "project.clj"
       slurp
       read-string
       (drop-while (fn [x] (not= x :quickcljs-view)))
       second))

(defmacro create-app-view []
  (if-let [sym (config-app-view-symbol)]
    `(do
       (require '~(symbol (namespace sym)))
       (defn ~'app-view []
         [~sym]))
    (throw (ex-info "Missing :quickcljs-view config in project.clj" {}))))

(defn start! []
  (let [http-port (let [port-file (io/file "target/http-port")]
                    (or (when (.exists port-file)
                          (Integer. (slurp port-file)))
                        (let [new-port (next-available-port)]
                          (spit port-file new-port)
                          new-port)))]
    (repl-api/start
      {:id "quickcljs"
       :options {:main "quickcljs.core"
                 :optimizations :none
                 :target :browser
                 :output-to "target/quickcljs-output/app.js"
                 :asset-path "/js/lib"
                 :output-dir "target/quickcljs-output/lib"
                 :closure-defines {"goog.DEBUG" true}
                 :build-paths [:watch-dirs :main]
                 :preloads ['quickcljs.core]
                 :parallel-build true
                 :verbose false}
       :config {:websocket-host :js-client-host
                ;:mode :serve
                :open-url false
                :watch-dirs ["src"]
                :ring-handler 'quickcljs.core/ring-handler
                :ring-server-options {:port http-port}}})

    (server/start-server
      {:accept 'quickcljs.core/io-repl ;'cljs.core.server/io-prepl
       :address "127.0.0.1"
       :port 6776
       :name "xyz"
       :args [:repl-env (repl-api/repl-env "quickcljs")]})

    #_(repl-api/cljs-repl "quickcljs")))



(defn stop! []
  (repl-api/stop-all))
