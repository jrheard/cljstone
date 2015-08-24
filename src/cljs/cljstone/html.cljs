(ns cljstone.html
  (:require [enfocus.core :as ef]
            [schema.core :as s])
  (:require-macros [enfocus.macros :as em])
  (:use [cljstone.minion :only [Minion]]
        [cljstone.hero :only [Hero]]
        [cljstone.board :only [BoardHalf]]))

(def root-template "resources/public/index.html")

(em/defsnippet hero-snippet :compiled "resources/public/index.html" ".hero div"
  [hero]
  ".name" (ef/content (:name hero)))

(em/defsnippet minion-snippet :compiled "resources/public/index.html" ".minion"
  [minion]
  ".name" (ef/content (:name minion))
  ".attack" (ef/content (str (:attack minion)))
  ".health" (ef/content (str (:health minion))))

(em/defsnippet board-half-snippet :compiled "resources/public/index.html" ".board-half"
  [board-half]
  ".hero" (ef/content (hero-snippet (:hero board-half)))
  ".minion-container" (ef/content
                        (map minion-snippet (:minions board-half))))

(em/deftemplate board-template :compiled "resources/public/index.html" [board-halves]
  ".board" (ef/content (map board-half-snippet board-halves)))

(defn draw-board [board]
  (ef/at "body" (ef/content (board-template [(:half-1 board) (:half-2 board)]))))
