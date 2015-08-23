(ns cljstone.app
  (:require [schema.core :as s]))

; types

(def Hero
  {:name s/Str
   :class s/Keyword
   :health s/Int})

(def Minion
  {:name s/Str
   :attack s/Int
   :health s/Int})

(def BoardHalf
  {:index s/Int
   :hero Hero
   :minions [Minion]})

(s/defn make-hero :- Hero [name hero-class]
  {:name name :class hero-class :health 30})

(def jaina (make-hero "Jaina" :mage))
(def thrall (make-hero "Thrall" :shaman))

(def chillwind-yeti {:name "Chillwind Yeti" :attack 4 :health 5})
(def goldshire-footman {:name "Goldshire Footman" :attack 1 :health 2})
(def magma-rager {:name "Magma Rager" :attack 5 :health 1})

(def p1 {:index 0 :hero jaina :minions [chillwind-yeti goldshire-footman]})
(def p2 {:index 1 :hero thrall :minions [magma-rager]})

; html utils

(defn get-nodes-by-selector
  ([selector] (get-nodes-by-selector selector js/document))
  ([selector node] (.querySelectorAll node selector)))

(defn set-html! [node content]
  (set! (. node -innerHTML) content))

; draw functions

(s/defn render-hero [hero :- Hero] (:name hero))

(s/defn render-minion
  [minion :- Minion]
  (str
    "<div class='minion'>"
    "<div class='name'>"
    (:name minion)
    "</div>"
    "<div class='attack'>"
    (:attack minion)
    "</div>"
    "<div class='health'>"
    (:health minion)
    "</div>"
    "</div>"))

(s/defn draw-board-half
  [board-half :- BoardHalf]
  (let [board-half-div (aget (get-nodes-by-selector ".board-half") (:index board-half))
        hero-div (aget (get-nodes-by-selector ".hero" board-half-div) 0)
        minions-div (aget (get-nodes-by-selector ".minion-container" board-half-div) 0)]
    (set-html! hero-div (render-hero (:hero board-half)))
    (set-html!
      minions-div
      (apply str (map render-minion (:minions board-half))))))

(draw-board-half p1)
(draw-board-half p2)
