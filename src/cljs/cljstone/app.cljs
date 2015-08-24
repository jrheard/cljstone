(ns cljstone.app
  (:require [enfocus.core :as ef]
            [enfocus.events :as ev]
            [schema.core :as s]
            [cljstone.board :as board]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html]))

(def p1 {:index 0 :hero hero/jaina :minions [minion/chillwind-yeti minion/goldshire-footman]})
(def p2 {:index 1 :hero hero/thrall :minions [minion/magma-rager]})

(html/draw-board [p1 p2])

; hm - do all characters need their own unique id, which we can use to look them up based on an event?
; main question is - how do we get from a drop event to a (origin-character, destination-character) tuple?

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
