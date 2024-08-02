(ns quickcljs.core
  (:require
    [clojure.walk :as walk]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [hiccup.core :as hiccup]
    [figwheel.main.api :as repl-api]
    [quickcljs.girouette :as giro])
  (:import
    (java.net ServerSocket)))

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
         [:title "QuickCLJS"]
         [:link {:rel "stylesheet"
                 :href "/css/twstyles.css"
                 :media "screen"}]]
        [:body
         [:div#app]
         [:script {:type "text/javascript" :src "/js/app.js"}]
         [:script {:type "text/javascript"} "quickcljs.core.init();"]]])}

    (= (request :uri) "/favicon.ico")
    {:status 200
     :body nil}

    (= (request :uri) "/css/twstyles.css")
    {:status 200
     :headers {"Content-Type" "text/css; charset=utf-8"
               "Cache-Control" "no-store"}
     :body (slurp (-> (request :uri)
                      (string/replace-first "/css/" "./target/quickcljs-output/css/"))) }

    (string/starts-with? (request :uri) "/js/")
    {:status 200
     :headers {"Content-Type" "application/javascript; charset=utf-8"
               "Cache-Control" "no-store"}
     :body (slurp (-> (request :uri)
                      (string/replace-first "/js/" "./target/quickcljs-output/")))}

    :else
    {:status 404}))

(defn config-app-view-symbol []
  ;; poor man's reading value from project.clj
  ;; it isn't aware of which profile is active
  (let [nodes (->> "project.clj"
                   slurp
                   read-string)]
    (or (->> nodes
             (drop-while (fn [x] (not= x :quickcljs-view)))
             second)
        (let [v (atom nil)]
          (->> nodes
               (walk/postwalk (fn [node]
                                (when (and
                                        (map-entry? node)
                                        (= :quickcljs-view (key node)))
                                  (reset! v (val node)))
                                node)))
          @v))))

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
                :mode :serve
                :open-url false
                :css-dirs ["target/quickcljs-output/css/"]
                :watch-dirs ["src" "dev-src"]
                :ring-handler 'quickcljs.core/ring-handler
                :ring-server-options {:port http-port}}})
    (giro/start! {})))

(defn stop! []
  (repl-api/stop-all))

