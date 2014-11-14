(ns patchwerk.core
  (:require [quil.core :refer :all]
            [quil.middleware :as m]))

;;; Adjustable variables
;;; ------
(def primary-color [255 255 255])
(def secondary-color [125 125 125])
(def tertiary-color [0 0 0])

(def square-size 64)

;;; Dimensions of quilt grid, i.e. number of squares
(def grid-width 6)
(def grid-height 6)

;;; Dimensions of sub grid within each quilt square.  Using a
;;; non-square set of dimensions will mess up errything.
(def sub-grid-width 2)
(def sub-grid-height 2)

;;; Derived variables
;;; ---------
(def sub-square-size (/ square-size sub-grid-width))

(def colors [primary-color secondary-color tertiary-color])

;;; Drawing helper functions
;;; ---------
(defn rand-choice
  [coll]
  (rand-nth coll))

(defn corner->vertices
  "Takes the coordinate of the northwest (top left) corner of the
  quilt square and returns a map containing the vertices of each of
  the corners."
  ([coord]
     (let [[x y] coord
           ne [(+ x square-size)
               y]
           sw [x
               (+ y square-size)]
           se [(+ x square-size)
               (+ y square-size)]]
       {:nw [x y] :ne ne :sw sw :se se}))
  ([coord size]
     (let [[x y] coord
           ne [(+ x size)
               y]
           sw [x
               (+ y size)]
           se [(+ x size)
               (+ y size)]]
       {:nw [x y] :ne ne :sw sw :se se})))

(defn triangle-lookup-table
  [corner-key]
  (case corner-key
    :ne [:se :nw]
    :nw [:ne :sw]
    :sw [:se :nw]
    :se [:ne :sw]))

;;; Drawing functions
;;; ---------
(defn draw-square
  "Accepts an origin coordinate `coord' in pixels and optionally a
  non-standard size `size' in pixels.  The origin coordinate is the
  top left corner, an [x y] coordinate.  If no size is given, defaults
  to the standard `square-size' size. Draws a single square."
  ([coord]
     (draw-square coord square-size))
  ([coord size]
     (let [[x y] coord
           w size
           h size]
       (rect x y w h))))

(defn draw-subsquares
  "`coord' is a vector [x y] of grid corner coordinates."
  [coord]
  (let [[x y] coord
        nw-subsquare-coord coord
        ne-subsquare-coord [(+ x sub-square-size)
                            y]
        sw-subsquare-coord [x
                            (+ y sub-square-size)]
        se-subsquare-coord [(+ x sub-square-size)
                            (+ y sub-square-size)]
        coords [nw-subsquare-coord
                ne-subsquare-coord
                sw-subsquare-coord
                se-subsquare-coord]]
    (doseq [c coords]
      ;; Choose a random fill color from the foreground or background
      ;; colors.  Could just use foreground, but adding background
      ;; will give some variety.
      (let [old-fill (current-fill)
            [r g b] (rand-choice colors)]
        (fill r g b)
        (draw-square c sub-square-size)
        (fill old-fill)))))

(defn draw-triangle
  "Draws a triangle to a random corner of the square defined by the
  `coord' in the :nw corner.  Optionally accepts an additional
  nonstandard size and corner keyword."
  ([coord]
     (let [vertices (corner->vertices coord)
           corner-key  (rand-nth (keys vertices))
           [x1 y1]  (corner-key vertices)
           [x2 y2] ((first (triangle-lookup-table corner-key)) vertices)
           [x3 y3] ((second (triangle-lookup-table corner-key)) vertices)]
       (triangle x1 y1 x2 y2 x3 y3)))
  ([coord size corner]
     (let [vertices (corner->vertices coord size)
           corner-key corner
           [x1 y1]  (corner-key vertices)
           [x2 y2] ((first (triangle-lookup-table corner-key)) vertices)
           [x3 y3] ((second (triangle-lookup-table corner-key)) vertices)]
       (triangle x1 y1 x2 y2 x3 y3))))

(def fish-funcs
  [ ;; nw
   (fn [coord]
     (let [size sub-square-size
           [x y] coord]
       (draw-square coord size)
       (draw-triangle [(+ x size) y] size :sw)
       (draw-triangle [x (+ y size)] size :ne)))
   ;; ne
   (fn [coord]
     (let [size sub-square-size
           [x y] coord]
       (draw-square [(+ x size) y] size)
       (draw-triangle coord size :se)
       (draw-triangle [(+ x size) (+ y size)] size :nw)))
   ;; se
   (fn [coord]
     (let [size sub-square-size
           [x y] coord]
       (draw-square [(+ x size) (+ y size)] size)
       (draw-triangle [(+ x size) y] size :sw)
       (draw-triangle [x (+ y size)] size :ne)))
   ;; sw
   (fn [coord]
     (let [size sub-square-size
           [x y] coord]
       (draw-square [x (+ y size)] size)
       (draw-triangle coord size :se)
       (draw-triangle [(+ x size) (+ y size)] size :nw)))])

(defn draw-fish
  "Draws a fish to a random corner of the square defined by the
  `coord'."
  [coord]
  (let [f (rand-choice fish-funcs)]
    (f coord)))

(def pattern-funcs
  "A map of patterns and corresponding functions that take a pixel
  coordinate `coord' as an argument and return a function to draw the
  appropriate pattern to a square of a quilt."
  {:square draw-square
   :triangle draw-triangle
   :subsquares draw-subsquares
   :fish draw-fish})

(def patterns (keys pattern-funcs))

;;; Set up grid
;;; ---------
(defn quilt-grid
  "Returns a list of coordinates specifying a `w' x `h' grid of
  ordinals."
  [w h]
  (for [i (range w) j (range h)]
    [i j]))

(def grid (quilt-grid grid-width grid-height))

(defn quilt-grid->pixels
  "Maps the pixel coordinates to the quilt grid coordinates for the
  grid squares of the size `square-size' "
  [g]
  (map (fn [coord]
         (let [[x y] coord]
           [(* x square-size) (* y square-size)]))
       g))

;;; Main quil functions
;;; ---------
(defn setup []
  (background 200)
  (no-loop))


(defn draw []
  (let [pixel-grid (quilt-grid->pixels grid)]
    ;; Set the background color to `primary-color'
    (let [[r g b] primary-color]
      (fill r g b))
    ;; Draw each of the squares for the background
    (doseq [coord pixel-grid]
      (draw-square coord))

    ;; Iterate through every square of the quilt.
    (doseq [coord pixel-grid]
      ;; Set the color to some random foreground color.
      (let [[r g b] (rand-choice colors)
            ;;  random choice to determine whether to subdivide
            ;; square, draw a fish, draw a single triangle or draw
            ;; triangles.
            draw-func ((rand-choice patterns) pattern-funcs)]
        (fill r g b)
        (draw-func coord))))
  (redraw))

(defsketch patchwerk-quilt
  :title "Patchwerk Quilt"
  :setup setup
  :draw draw
  :size [(* square-size grid-width) (* square-size grid-height)])
