# A Reagent Modal Dialog #

How to implement a very lightweight modal dialog in `Reagent`

Background
----------

There is already a [recipe][Reagent-Bootstrap-Modal] for `Reagent`
modal windows using `Bootstrap`.

Below is an alternative that follows the approach posted by the
[W3schools][W3SchoolsModal].

Differences from [W3schools][W3SchoolsModal]:

* No DOM node manipulation--we rely on `Reagent` atoms instead
* Styling done using [`tailwindcss`][tailwindcss]: this is not a
  requirement; please take a look at the [W3schools][W3SchoolsModal]
  code and use whatever approach you'd like for CSS

[W3SchoolsModal]: https://www.w3schools.com/howto/tryit.asp?filename=tryhow_css_modal "W3schools Modal"
[tailwindcss]: https://tailwindcss.com/ "tailwindcss"
[Reagent-Bootstrap-Modal]: https://github.com/reagent-project/reagent-cookbook/tree/master/recipes/bootstrap-modal "Bootstrap modal window"

# Some Comments on the Code #

As mentioned above, we use a `Reagent` atom to hold the value of the
CSS `display` attribute: the modal is hidden when this value is
`"none"` and revealed when we change the value to `"block"`. 

```clojure
(def modal-display (r/atom {:background-color "rgba(0,0,0,0.4"
                            :display "none"}))
```

I haven't been able to work out how to implement the same opacity
effect in `tailwindcss` with a utility class, so we can use the CSS
attribute to set the backgroun color and keep it in the same atom. One
could also add the color to the `tailwindcss` palette (see the
documentation for `tailwindcss`) or create a custom class for the
modal background an set the `background-color` there, in which case we
could also use it toset all the other CSS properties.

Implementing the modal dialog: the main idea is to have a background
`div` spanning the entire window (position fixed, anchored at 0,0,
width and height at 100%) that we can use to darken all the elements
behind the modal dialog.

The modal dialog itself in this example is a `div` holding a `p` and a
`button`:

```clojure
    ;; modal background container
    [:div
     {:class "fixed z-10 top-0 left-0 w-full h-full overflow-auto" 
      :style @modal-display}
     ;; modal content container
     [:div
      {:class "mx-auto p-4 modal-content flex items-center w-5/6 mt-32
               bg-gray-500 border border-solid border-black"
       :on-click #(.stopPropagation %)} ; disable closing inside modal
      [:p
       {:class "w-11/12"} "Some Text in the Modal..."]
      ;; button to close modal
      [:button
       {:class "px-3 py-2 ml-auto border border-solid border-black
                rounded-md bg-gray-400 hover:bg-gray-300 focus:bg-gray-300"
        :on-click #(put! event-queue [:hide-modal])} 
       "Close"]]]]])
```

The modal background container is displayed whenever the
`modal-display` atom changes to `block`. This is done by clicking on
the `open modal` button:

```clojure
[:button.m-2.p-2.border.border-solid.border-black.rounded-md.bg-gray-200
     {:on-click #(do (put! event-queue [:show-modal])
                     (.activeElement.blur js/document)
                     (.stopPropagation %))} "Open Modal"]
```

We change the value of the atom by putting a `:show-modal` event on
the event queue (a simple-minded `core.async` implementation of the Flux
Architecture). Events are dispatched thusly:

```clojure
(def event-queue (chan))

(go-loop [[event payload] (<! event-queue)]
  (case event
    :show-modal (swap! modal-display #(assoc % :display "block"))
    :hide-modal (swap! modal-display #(assoc % :display "none")))
  (recur (<! event-queue)))
```

Note: you don't have to implement a queue to change the value of
`modal-atom`---you could instead change the value of the atom directly
in the function associated with the `:on-click` event. I like the
`core.async` approach because it keeps all the code mutating state in
one place.

# How to Run the Code #

These directions assume that you have `npm` and the proper
ClojureScript setup (Java, etc.)

1. Install with `npm`: `npm install`
2. Build CSS files: `npm run tw`
3. Open `main.cljs` in Emacs
4. `cider-jack-in-cljs`:
   * Select `shadow` for `cljs` REPL
   * Select `:app` as the build
   * Type `y` to start a browser session
   
Notice that the app might start *before* the code is compiled by
`cider` if you don't wait long enough before you start the browser
session, so you might get a message in the browser that the client is
stale. All you need to do is refresh the browser to get rid of the
error message

# Credit #

This repository leverages the work of Michael Zamansky and differs
from his `Reagent` template thusly:

* Updated to `reagent 0.10.0` and `core.async 1.0.567`
* Corresponding upgrades to `react` and `react-dom`
* `package.json` includes `onchange`
* Minor upgrade to `tailwindcss` to get rid of a low-level
  vulnerability

