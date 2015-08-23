(ns cljstone.app
  (:require [schema.core :as s]))

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

(s/defn make-hero :- Hero
  [name :- s/Str
   class :- s/Keyword]
  {:name name :class class :health 30})

(def jaina (make-hero "Jaina" :mage))
(def thrall (make-hero "Thrall" :shaman))

(defn get-nodes-by-selector [selector]
  (.querySelectorAll js/document selector))

(defn set-text! [node content]
  (set! (. node -textContent) content))

(s/defn draw-board-half
  [board-half :- BoardHalf]
  (js/console.log (:index board-half))
  (let [div (aget (get-nodes-by-selector ".board-half") (:index board-half))]
    (set-text! div (:name (:hero board-half)))))

(def p1 {:index 0 :hero jaina :minions []})
(def p2 {:index 1 :hero thrall :minions []})

(draw-board-half p1)
(draw-board-half p2)
