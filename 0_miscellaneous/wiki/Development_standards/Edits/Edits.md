Edits are the form of actions taken by the user, but represented as instance of a class extending
````PNEdit````. Examples of this can be the action "Creating a decision node" which is represented
by ````AddNodeEdit````, or "Adding a link from node A to node B" which is represented by
````AddLinkEdit````.

This allows to build a history of the actions made by the user, so he can undo actions with
````Ctrl + Z```` and redo them with ````Ctrl + Y````. This can be seen in practice in the following
video:

https://github.com/user-attachments/assets/088e6d89-f70d-4dcb-a57c-335c51d0c984

To make an edit to take place, you only need to find the right edit, create an instance of it, and
then calling the ````execute```` method. For example, this would add a chance node in the network.

```java
new AddNodeEdit(probNet, variable, NodeType.CHANCE).executeEdit();
```

When executing the edit, a few underlying actions take place: First the edit will check it won't
violate any constraint via the ``PNEdit#checkConstraintsWillBeMet``[^1], and then it will call
``PNEdit#doEdit()`` for the action to take place[^2]. Once the action has been done, it
gets added[^3] to the Edits History to allow the user to use ````Ctrl + Z````.

[^1]: _If ``PNEdit#checkConstraintsWillBeMet()`` throws a ``ConstraintViolatedException``, the 
execution will be halted_.

[^2]: _If ``PNEdit#doEdit()`` throws a ``DoEditException``, the execution will be halted_.

[^3]: _Adding an edit to the history removes all of the edits that can be currently redone by using
````Ctrl + Y````._