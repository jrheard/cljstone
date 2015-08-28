(ns cljstone.html
  (:require [reagent.core :as r]
            [schema.core :as s])
  (:use [cljstone.minion :only [Minion get-attack get-health]]
        [cljstone.hero :only [Hero]]
        [cljstone.board :only [BoardHalf perform-attack]]))

(defn draw-hero [hero]
  [:div.hero
   [:div.name (:name hero)]])

(defn draw-minion [minion]
  [:div.minion {:data-minion-id (:id minion)
                :draggable true
                :on-drag-start #(js/console.log "dragstart")
                :on-drag-over #(.preventDefault %)
                :on-drop #(js/console.log "drop")}
   [:div.name (:name minion)]
   [:div.attack (get-attack minion)]
   [:div.health (get-health minion)]])

(defn draw-board-half [board-half characters-by-id]
  [:div.board-half
   [:div.body
     [draw-hero (:hero board-half)]
     [:div.minion-container
      (for [minion (:minions board-half)]
        ^{:key (:id minion)} [draw-minion minion])]]])

(defn draw-board [board-atom]
  (let [board @board-atom]
    [:div.board
     [draw-board-half (:half-1 board) (:characters-by-id board)]
     [draw-board-half (:half-2 board) (:characters-by-id board)]]))


(defn mount-reagent [board-atom]
  (r/render-component [draw-board board-atom]
                      (js/document.getElementById "content")))

(defn get-data-transfer [goog-event]
  (-> goog-event
      .getBrowserEvent
      .-dataTransfer))

(defn get-target-dataset [goog-event]
  (-> goog-event
      .-currentTarget
      .-dataset))


; ok in om we're gonna be storing actual Minions in BoardHalfs instead of minion ids, most likely
; todo - look into om's drag and drop before making that decision

#_(defn draw-board [board]
  (ef/at "body" (ef/content (board-template @board)))

  (ef/at ".minion" (ev/listen :dragstart
                              (fn [e]
                                (js/console.log "dragstart")
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
                                  (swap! board perform-attack origin-minion-id destination-minion-id)
                                  (draw-board board)
                                  (.preventDefault e))))))



