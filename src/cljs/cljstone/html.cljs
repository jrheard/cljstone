(ns cljstone.html
  (:require [goog.dom :as dom]
            [reagent.core :as r]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:use [cljs.core.async :only [chan <! >! put!]]
        [cljs.pprint :only [pprint]]
        [cljstone.minion :only [get-attack get-health can-attack]]
        [cljstone.board :only [end-turn play-card path-to-character]]
        [cljstone.combat :only [attack]]))

(s/defschema InputEvent {:foo s/Any})

(defn- get-character-id-from-event [event]
  (-> event
      .-currentTarget
      .-dataset
      .-characterId
      js/parseInt))

(defn async-some [predicate input-chan]
  (go-loop []
    (let [msg (<! input-chan)]
      (if (predicate msg)
        msg
        (recur)))))

(defn get-next-message [msg-type-set input-chan]
  (async-some #(contains? msg-type-set (:type %)) input-chan))

(defn draw-minion-card [card]
  [:div.content
   [:div.name (:name card)]
   [:div.attack (:attack card)]
   [:div.health (:health card)]])

(defn draw-spell-card [card]
  [:div.content
   [:div.name (:name card)]])

(defn draw-card [card index player board-atom is-owners-turn input-chan]
  (let [playable is-owners-turn ; will become more complex later
        classes (str
                  "card "
                  (clj->js (:class card))
                  (condp = (:type card) :minion " minion " :spell " spell ")
                  (when playable "playable"))]
    [:div {:class classes
           :data-card-index index
           :on-click (fn [e]
                       (when playable
                         (swap! board-atom play-card player index)))}
     (condp = (:type card)
       :minion [draw-minion-card card]
       :spell [draw-spell-card card])]))

(defn draw-hero [hero]
  [:div.hero
   [:div.name (:name hero)]])

(defn draw-minion [minion board board-atom is-owners-turn input-chan]
  ; XXXXXX have this also depend on the state of the board being :default
  (let [minion-can-attack (and is-owners-turn (can-attack minion))
        classes (str
                  "minion "
                  (when minion-can-attack "can-attack"))]
    [:div {:class classes
           :data-character-id (:id minion)
           :draggable minion-can-attack
           :on-drag-start (fn [e]
                           (let [character-id (get-character-id-from-event e)]
                             (.setData (.-dataTransfer e) "text/plain" character-id)))
           :on-drag-over (fn [e]
                           (let [origin-character-id (js/parseInt (.getData (.-dataTransfer e) "text/plain"))
                                 destination-character-id (get-character-id-from-event e)]
                             (when (not= (first (path-to-character board origin-character-id))
                                         (first (path-to-character board destination-character-id)))
                               (.preventDefault e))))
           :on-drop (fn [e]
                     (let [origin-character-id (js/parseInt (.getData (.-dataTransfer e) "text/plain"))
                           destination-character-id (get-character-id-from-event e)]
                       (put! input-chan {:type :attack
                                         :origin-id origin-character-id
                                         :destination-id destination-character-id})
                       (.preventDefault e)))}
     [:div.name (:name minion)]
     [:div.attack (get-attack minion)]
     [:div.health (get-health minion)]]))

(defn draw-board-half [board board-atom player whose-turn input-chan]
  (let [board-half (board player)
        is-owners-turn (= whose-turn player)]
    [:div.board-half
     [:div.hand
      [:h3 (:name (:hero board-half))]
      (for [[index card] (map-indexed vector (:hand board-half))]
        ^{:key (:id card)} [draw-card card index player board-atom is-owners-turn input-chan])]
     [:div.body
       [draw-hero (:hero board-half)]
       [:div.minion-container
        (for [minion (:minions board-half)]
          ^{:key (:id minion)} [draw-minion minion board board-atom is-owners-turn input-chan])]]]))

(defn draw-end-turn-button [board board-atom input-chan]
  [:div.end-turn {:on-click (fn [e]
                              (swap! board-atom end-turn))}
   "End Turn"])

(defn draw-combat-log-entry [board entry]
  [:div.log-entry
   (str
     (-> entry :target :name)
     " was attacked for "
     (- (-> entry :modifier :effect :health))
     " damage")])

(s/defn draw-combat-log [board]
  (let [combat-log (:combat-log board)]
    [:div.combat-log-viewport
     [:div.combat-log
     (for [entry combat-log]
       ^{:key (:id entry)} [draw-combat-log-entry board entry])]]))

(defn draw-board [board-atom input-chan]
  (let [board @board-atom]
    [:div.board
     [draw-board-half board board-atom :player-1 (board :whose-turn) input-chan]
     [draw-board-half board board-atom :player-2 (board :whose-turn) input-chan]
     [draw-end-turn-button board board-atom input-chan]
     [draw-combat-log board]
     [:div.turn (pr-str (:whose-turn board)) (pr-str (:turn board))]]))

(defn draw-board-atom [board-atom]
  (let [input-chan (chan)]
    ; xxx this go-loop seems a bit superfluous for now, but will become more useful when we have several different types of events coming in on input-chan
    (go-loop []
      (let [{:keys [origin-id destination-id]} (<! (get-next-message #{:attack} input-chan))]
        (swap! board-atom attack origin-id destination-id)
      (recur)))

    (r/render-component [draw-board board-atom input-chan]
                        (js/document.getElementById "content"))))
