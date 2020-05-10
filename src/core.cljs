(ns quickcljs.core
  (:require
    [reagent.dom :as rdom])
  (:require-macros
    [quickcljs.core :refer [create-app-view]]))

(create-app-view)

(enable-console-print!)

(defn render
  []
  (rdom/render
    [app-view]
    (js/document.getElementById "app")))

(defn ^:export init
  []
  (.addEventListener js/document
                     (name :figwheel.after-load)
                     (fn [_]
                       (render)))
  (render))


