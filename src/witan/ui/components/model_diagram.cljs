(ns witan.ui.components.model-diagram
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent defcomponentmethod]]
            [thi.ng.geom.svg.core :as svg]
            [sablono.core :as sablono]))

;; All values will used in SVG without units.
(def render-options
  {:box-height        30
   :box-h-spacing     20
   :box-width         125
   :box-w-spacing     75
   :group-box-padding 6
   :canvas-width      600
   :padding           10})

(defn stacked-box
  "Draws the ith box in a stack starting at (x-offset, y-offset), with class cl."
  [render-options x-offset y-offset cl i]
  (let [{:keys [box-height box-width box-h-spacing]} render-options]
    (svg/rect [x-offset (+ (* (+ box-height box-h-spacing) i) y-offset)]
              box-width
              box-height
              {:class cl
               :key (str "box-" x-offset "-" i)})))

(defn stack-of-boxes
  "Draws a stack of n-boxes boxes starting at x-offset, with y position determined so as to be centred
  on a stack with max-n boxes in it. Boxes will have class cl."
  [render-options x-offset max-n n-boxes cl]
  (let [{:keys [box-height box-h-spacing]} render-options]
    (mapv (partial stacked-box
                   render-options
                   x-offset
                   (* (/ (+ box-height box-h-spacing) 2)
                      (- max-n n-boxes))
                   cl)
          (range n-boxes))))

(defn stacked-line
  "Similar to the above, draws the ith line in a stack, starting at y-offset, running from x-start to x-end."
  [render-options x-start x-end y-offset i]
  (let [{:keys [box-height box-width box-h-spacing]} render-options
        line-y (+ (* (+ box-height box-h-spacing) i) (/ box-height 2) y-offset)]
    (svg/line [x-start line-y]
              [x-end line-y]
              {:class "line"
               :key (str "line-" x-start "-" i)})))

(defn stack-of-lines
  "See stack-of-boxes."
  [render-options x-start x-end max-n n-lines]
  (let [{:keys [box-height box-h-spacing]} render-options]
    (mapv (partial stacked-line
                   render-options
                   x-start
                   x-end
                   (* (/ (+ box-height box-h-spacing) 2)
                      (- max-n n-lines)))
          (range n-lines))))

(defn group-box
  "Draws a box that groups together a number of boxes. Is drawn relative to a stack of boxes starting at
  (x-offset, y-offset) containing boxes from i-start to i-end."
  [render-options x-offset y-offset i-start i-end]
  (let [{:keys [box-height box-width box-h-spacing group-box-padding]} render-options]
    (svg/rect [(- x-offset group-box-padding)
               (- (+ (* (+ box-height box-h-spacing) i-start) y-offset) group-box-padding)]
              (+ box-width (* 2 group-box-padding))
              (+ (* (+ box-height box-h-spacing) (- i-end i-start))
                 (* -1 box-h-spacing)
                 (* 2 group-box-padding))
              {:class "group"
               :key (str "group-box-" i-start)})))

(defn group-ranges
  "Given a list of group sizes, gives the start and end indices of each group i.e.
  [2 2 3] -> ((0 2) (2 4) (4 7)). Used for drawing the output grouping boxes."
  [n-outputs]
  (let [c (reductions + 0 n-outputs)]
    (apply map list [(drop-last c) (rest c)])))

(defn group-boxes
  "Draws a set of group boxes, specified by group-sizes (see group-ranges)."
  [render-options x-offset max-n group-sizes]
  (let [{:keys [box-height box-h-spacing]} render-options
        ranges (group-ranges group-sizes)
        n-boxes (apply + group-sizes)]
    (mapv #(group-box
            render-options
            x-offset
            (* (/ (+ box-height box-h-spacing) 2) (- max-n n-boxes))
            (first %)
            (second %))
          ranges)))

(defn render-model
  "Draws a digram for a given model specification."
  [render-options model]
  (let [{:keys [canvas-width box-width box-w-spacing box-height box-h-spacing padding]} render-options
        {:keys [n-inputs n-outputs]} model
        total-outputs (apply + n-outputs)
        max-n (max n-inputs total-outputs)]
    (sablono/html (svg/svg
                    {:width  (+ canvas-width (* 2 padding))
                     :height (+ (* max-n (+ box-height box-h-spacing)) (* 2 padding))}
                    (svg/group
                      {:transform (str "translate(" padding ", " padding ")")
                       :key "model-diagram-g"}
                      (concat
                        (stack-of-boxes render-options 0 max-n n-inputs "input")
                        (stack-of-lines render-options
                                        box-width
                                        (+ box-width box-w-spacing)
                                        max-n
                                        n-inputs)
                        [(svg/rect [(+ box-width box-w-spacing) 0]
                                   box-width
                                   (- (* max-n (+ box-height box-h-spacing)) box-h-spacing)
                                   {:class "model" :key "model-box"})]
                        (group-boxes
                          render-options
                          (* 2 (+ box-width box-w-spacing))
                          max-n
                          n-outputs)
                        (stack-of-boxes render-options
                                        (* 2 (+ box-width box-w-spacing))
                                        max-n
                                        total-outputs
                                        "output")
                        (stack-of-lines render-options
                                        (+ (* 2 box-width) box-w-spacing)
                                        (* 2 (+ box-width box-w-spacing))
                                        max-n
                                        total-outputs)))))))

(defcomponent diagram
  [model-shape owner]
  (render [this]
    (render-model render-options model-shape)))