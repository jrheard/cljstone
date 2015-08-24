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

(defn get-data-transfer [goog-event]
  (-> goog-event
      .getBrowserEvent
      .-dataTransfer))

(defn get-target-dataset [goog-event]
  (-> goog-event
      .-currentTarget
      .-dataset))

; TODO: record the id of the dragged minion and also use the one of the dropped minion
(ef/at ".minion" (ev/listen :dragstart
                            (fn [e]
                              (let [minion-id (.-minionId (get-target-dataset e))
                                    data-transfer (get-data-transfer e)]
                                (.setData data-transfer "text/plain" minion-id)))))

(ef/at ".minion"
  (ev/listen :dragover
             (fn [e]
               (.preventDefault e))))

(ef/at ".minion" (ev/listen :drop
                            (fn [e]
                              (let [origin-minion-id (.getData (get-data-transfer e) "text/plain")
                                    destination-minion-id (.-minionId (get-target-dataset e))]
                                (js/console.log origin-minion-id)
                                (js/console.log destination-minion-id)
                                (.preventDefault e)))))
