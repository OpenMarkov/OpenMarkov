When adding a PNEditListener to the ProbNet's listener (via
```probNet.getPNESupport().addListener(*Your PNEditListener*)```), said listener will be able to
react to certain events triggered by the edit. For this, the listener must implement the methods
that match the events it wants to listen to.

The list of events is the following:

- ``beforeEditExecutes(PNEdit)``: Happens before trying to execute the edit.
- ``afterEditExecutes(PNEdit)``: Happens after successfully executing the edit.
- ``onEditViolatesConstraints(PNEdit, ConstraintViolatedException)``: Happens before trying to
  execute the edit, and it means a constraint would be violated if this edit was to be executed.
  <br>If this event happens, then the execution of the edit won't take place.
  <br>If this event happens, then it will be impossible to reach the events ``beforeEditExecutes``,
  ``afterEditExecutes``, and ``onEditFailed``.
- ``onEditFailed(PNEdit, DoEditException)``: When an edit was tried to be executed, but the
  ```PNEdit#doEdit()``` operation threw a ```DoEditException```.
  <br>If this event happens, then the edit might have taken place if the implementation of the edit
  was badly implemented.
  <br>If this event happens, then it will be impossible to reach the event ``afterEditExecutes``,
  but the event ``beforeEditExecutes`` is guaranteed to have happened.
- ``afterUndoingEdit(PNEdit edit)``: Happens after undoing edit.
  <br>The undo operation shall not fail, and therefore there is no ``beforeUndoingEdit(PNEdit edit)``
  event.
- ``afterRedoingEdit(PNEdit edit)``: Happens after redoing edit.
  <br>The redo operation shall not fail, and therefore there is no ``beforeRedoingEdit(PNEdit edit)``
  event.
