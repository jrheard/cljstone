(ns cljstone.html
  (:require [cljstone.board :as board]
            [cljstone.hero :as hero]
            [cljstone.minion :as minion]
            [dommy.core :as dommy]
            [schema.core :as s]))

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
  (let [board-half-div (nth (dommy/sel [".board-half"]) (:index board-half))
        hero-div (dommy/sel1 board-half-div ".hero")
        minions-div (dommy/sel1 board-half-div ".minion-container")]
    (dommy/set-html! hero-div (render-hero (:hero board-half)))
    (dommy/set-html!
      minions-div
      (apply str (map render-minion (:minions board-half))))))
