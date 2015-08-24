(ns cljstone.app
  (:require [enfocus.core :as ef]
            [enfocus.events :as ev]
            [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [make-board summon-minion]]))

(def board (-> (make-board hero/jaina hero/thrall)
               (summon-minion :half-1 minion/chillwind-yeti)
               (summon-minion :half-2 minion/magma-rager)
               (summon-minion :half-1 minion/goldshire-footman)))

(html/draw-board board)

; TODO: record the id of the dragged minion and also use the one of the dropped minion
(ef/at ".minion" (ev/listen :dragstart
                            (fn [e]
                              (js/console.log e)
                              (.setData (.-dataTransfer (.-event_ e))
                                        "source-thing"
                                        (.-target e)))))

(ef/at ".minion" (ev/listen :drop
                            (fn [e]
                              (.preventDefault e)
                              (.stopPropagation e)
                              (js/console.log (.getData (.-dataTransfer e) "source-thng")))))
