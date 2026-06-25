## Adding an item to a menu
##### The item and the action command
If you are creating an item you surely want it to execute an action. If you flip the switch, you expect it to turn on the lights, either with the switch next to the door or the switch above your bed. That is important, you can have ten different switches (items) but the command is one: "turn on, lights!".

Normally you have to *create the item/s and the action command*. This tutorial explains how to create both and *how to enable the action command*.

#### **Where to add and when to enable**
There are three places to add a menu item: the main menu, a toolbar or a contextual menu. Each of them enables/disables its action command using setOptionEnabled() in another place:

| Where do you want it?       | Main menu                                                 | Toolbar                                                   | Contextual menu                           |
|-----------------------------|-----------------------------------------------------------|-----------------------------------------------------------|-------------------------------------------|
| To add the item modify:      | `MainMenu` class                                           | The specific class of the toolbar                         | The specific class of the contextual menu |
| To enable/disable it modify: | The `updateOptions()` methods of `MainPanelMenuAssistant` class | The `updateOptions()` methods of `MainPanelMenuAssistant` class | The specific class of the contextual menu |
| Listener of the menu item: | The `actionPerformed()` method of `MainPanelListenerAssistant` | The `actionPerformed()` method of `MainPanelListenerAssistant` | The `actionPerformed()` method of `MainPanelListenerAssistant` |

#### **Adding**
> You can easily do every step looking at the code of other menu items in the same menu.

Locate the desired menu class, these are the possible ones as for now (june 2018):

```
    * MainMenu  

    * StandardToolBar
    * EditionToolBar
    * InferenceToolBar

    * LinkContextualMenu  
    * NodeContextualMenu  
    * NetworkContextualMenu  
    * InstanceContextualMenu  
    * UncertaintyContextualMenu
```

2. Create the menu item as an attribute in the menu class.

3. Create a private getter for it. This getter should initialize the menu item by defining its name, action command and listener, if it is a toolbar, also the icon, tooltip, and other toolbar specifics.

    2.1. For the name create a string attribute in the class that acts as a list of names: `MenuItemNames`. Use already created items in the menu to place this adding in order to keep it sorted and be consistent with the other modifications, for instance, if I created a "removeNodeAndChildsMenuItem" I would make every modification after the "removeMenuItem" ones.

    2.2. Then create an xml entry for the name in each localized language (That is, in `menus_en.xml` and `menus_es.xml`)

    2.3. For the action command create a string attribute in the class `ActionCommands`. That class acts as a list of action commands . Again, use already created items to place the entry.

    2.4 \[*Only for toolbar*\] Set the icon path in the icon loader list (the same as names and action commands, remember the localization). Put the icon image in the resorce folder. Set focusable to false. Set the tooltip (and localize it) and program the mouse move listener to reset it.

4. Create an entry for your item in the protected getter of the menu: `getJComponentActionCommand()`.

5. Create an `else if` clause in the `actionPerformed()` of `MainPanelListenerAssistant` that activates when the action command of your menu item is detected (you clicked the item). The code inside will be run then. *This effectively links the button with the action it produces.* Common options for running code there are  [running an edit](Edits/Edits.md#creating-an-edit) or running a function from the editor panel, which you do by:

    4.1 Calling a function from the current `NetworkPanel` with: `getCurrentNetworkPanel().<yourFunction>();`

    4.2 Creating that function on the class `NetworkPanel` which, in turn, calls a function from the
current `EditorPanel` with: `editorPanel.<yourFunction>();`

    4.3 Creating that function on the class `EditorPanel` and add the desired code there.

If you miss the last step, the menu will show your option and make it available when the constraint is met, but clicking on it will render a message like `Not implemented function`.



Here is a table that summarizes the modifications you should made:

| **File**                  | **Modifications**                                                                     |
|---------------------------|---------------------------------------------------------------------------------------|
| Menu class     | Declare menu item                                                                     |
|                           | Add the item to the menu in initialization methods                                             |
|                           | Create a private getter that initializes the item                                     |
|                           | Create an entry in the protected getter                                 |
| Listener class| Create an `else if` clause for your action command to run some code |
| | \[*Only toolbar*\] Set icon, tooltip and mouselistener |
| Action command list class | Create the action command                                       |
| Name list class           | Create the name                                                                       |
| \[*Only toolbar*\] Icon loader | Create the icon path |
| XML localization files    | Localize the name (and other toolbar strings like tooltip) in every language                                                   |

#### **Enabling/disabling action commands**
##### The updateOptions methods
These methods decide if each option should be active after the event they describe happen. Some examples are _NetworkModified_ or _AllNetworksClosed_.

Enabling is different in contextual menus because they are created on the click while the main menu and the toolbars are always there (even if you don't see them). So if an action command is called from both the main menu and a contextual menu, you only need to write your code in the `updateOptions()` methods.

##### Enabling in main menu and toolbars
Go to `MainPanelMenuAssistant` and go through the `updateOption()` methods, adding a `setOptionEnabled` with your action command when necessary. If you need to test for more conditions, add a separate class `yourActionValidator` that checks them in the constraints folder of the gui module (search for `ValidName` class or `ArcReversalValidator` class as some examples of validator classes).

##### Enabling in contextual menus
Do it in the constructor of the menu class, after the initialize method. First, add a separate class `yourActionValidator` that checks your conditions in the constraints folder of the gui module (search for `ValidName` class or `ArcReversalValidator` class). Then make the constructor of the menu call this test and enable/disable the menu item.