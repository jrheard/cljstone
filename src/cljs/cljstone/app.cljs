(ns cljstone.app
  (:require [schema.core :as s]
            [cljstone.board :as board]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html]))

(def p1 {:index 0 :hero hero/jaina :minions [minion/chillwind-yeti minion/goldshire-footman]})
(def p2 {:index 1 :hero hero/thrall :minions [minion/magma-rager]})

(html/draw-board-half p1)
(html/draw-board-half p2)
