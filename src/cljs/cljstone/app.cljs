(ns cljstone.app
  (:require [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [Board make-board summon-minion]]))

(def board (atom (-> (make-board hero/jaina hero/thrall)
                     (summon-minion :half-1 minion/chillwind-yeti)
                     (summon-minion :half-2 minion/magma-rager)
                     (summon-minion :half-1 minion/goldshire-footman))))

(html/draw-board board)
