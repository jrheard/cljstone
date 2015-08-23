(ns cljstone.html
  (:require [schema.core :as s]
            [cljstone.board :as board]
            [cljstone.hero :as hero]
            [cljstone.minion :as minion]))

(defn get-nodes-by-selector
  ([selector] (get-nodes-by-selector selector js/document))
  ([selector node] (.querySelectorAll node selector)))

(defn set-html! [node content]
  (set! (. node -innerHTML) content))

; draw functions

(s/defn render-hero [a-hero :- hero/Hero] (:name a-hero))

(s/defn render-minion
  [a-minion :- minion/Minion]
  (str
    "<div class='minion'>"
    "<div class='name'>"
    (:name a-minion)
    "</div>"
    "<div class='attack'>"
    (:attack a-minion)
    "</div>"
    "<div class='health'>"
    (:health a-minion)
    "</div>"
    "</div>"))

(s/defn draw-board-half
  [board-half :- board/BoardHalf]
  (let [board-half-div (aget (get-nodes-by-selector ".board-half") (:index board-half))
        hero-div (aget (get-nodes-by-selector ".hero" board-half-div) 0)
        minions-div (aget (get-nodes-by-selector ".minion-container" board-half-div) 0)]
    (set-html! hero-div (render-hero (:hero board-half)))
    (set-html!
      minions-div
      (apply str (map render-minion (:minions board-half))))))
