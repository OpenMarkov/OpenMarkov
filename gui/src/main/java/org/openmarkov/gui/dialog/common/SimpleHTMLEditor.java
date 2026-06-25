package org.openmarkov.gui.dialog.common;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.loader.element.ImageLoader;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This Panel contains both a foreground area where an user can create and see Styled Documents in HTML format, allowing them
 * to forget specifics on how to create an HTML document.
 * <p>
 * Among the utilities it offers, a toolbar with buttons and other elements is provided to allow the user to modify the
 * document.
 *
 * @author jrico
 */
public final class SimpleHTMLEditor extends JPanel {
    
    private static final int EDITOR_DIMENSION_MIN_HEIGHT = 300;
    private static final float UI_BUTTONS_FONT_SIZE = 14.0f;
    private static final List<String> FONT_SIZES = IntStream.rangeClosed(0, 120)
                                                            .filter(n -> n % 2 == 0 && n > 0)
                                                            .mapToObj(String::valueOf)
                                                            .toList();
    private static final int DEFAULT_FONT_SIZE_SELECTION = 12;
    
    private final JEditorPane editorPane;
    private final JToolBar toolBar;
    
    /**
     * Creates an HTML Editor with an initial content.
     */
    public SimpleHTMLEditor(String initialHTMLContent) {
        this.editorPane = new JEditorPane();
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        this.editorPane.setContentType("text/html");
        this.editorPane.setEditorKit(htmlEditorKit);
        this.editorPane.setText(initialHTMLContent);
        this.editorPane.setCaretPosition(0);
        this.editorPane.setEditable(true);
        new OpenHyperlinkOnSelectionAction(this.editorPane).setAsListenerOnEditor();
        
        this.toolBar = new JToolBar();
        this.toolBar.setRollover(true);
        this.toolBar.setFloatable(false);
        
        var commonComponents = new CommonComponents(this, this.editorPane, htmlEditorKit, this.toolBar);
        
        this.addToolBarUIComponents(commonComponents);
        
        this.setLayout(new BorderLayout());
        this.add(this.toolBar, BorderLayout.PAGE_START);
        this.add(new JScrollPane(this.editorPane), BorderLayout.CENTER);
    }
    
    /**
     * Creates and adds the elements like buttons over the toolbar.
     * @param commonComponents Common elements every component should have access to.
     */
    @SuppressWarnings("OverlyLongMethod")
    private void addToolBarUIComponents(CommonComponents commonComponents) {
        URL copyIconImageURL = Objects.requireNonNull(SimpleHTMLEditor.class.getResource("/htmleditor/copybutton.png"));
        JButton copyUI = new JButton(ImageLoader.load(copyIconImageURL));
        copyUI.addActionListener(new DefaultEditorKit.CopyAction());
        
        JButton cutUI = SimpleHTMLEditor.createJButton("✂");
        cutUI.addActionListener(new DefaultEditorKit.CutAction());
        JButton pasteContentUI = SimpleHTMLEditor.createJButton("\uD83D\uDCCB");
        pasteContentUI.addActionListener(new DefaultEditorKit.PasteAction());
        
        URL iconResource = Objects.requireNonNull(SimpleHTMLEditor.class.getResource("/htmleditor/hyperlink_add.png"));
        JButton addHyperlinkUI = new JButton(ImageLoader.load(iconResource));
        addHyperlinkUI.addActionListener(new AddHyperlinkAction());
        
        JButton makeBoldUI = SimpleHTMLEditor.createMakeBoldUI(commonComponents);
        JButton makeItalicUI = SimpleHTMLEditor.createMakeItalicUI(commonComponents);
        JButton makeUnderStrikedUI = SimpleHTMLEditor.createMakeUnderStrikedUI(commonComponents);
        JComboBox<String> changeFontSizeUI = SimpleHTMLEditor.createChangeFontSizeUI(commonComponents);
        JComboBox<String> changeFamilyFontUI = SimpleHTMLEditor.createChangeFamilyFontUI(commonComponents);
        JButton changeForegroundColorUI = SimpleHTMLEditor.createChangeForegroundColorUI(commonComponents);
        
        JButton setLeftAlignmentUI = SimpleHTMLEditor.createAlignmentUI(commonComponents, StyleConstants.ALIGN_LEFT, "align-left", "/htmleditor/leftalign.png");
        JButton setCenterAlignmentUI = SimpleHTMLEditor.createAlignmentUI(commonComponents, StyleConstants.ALIGN_CENTER, "align-center", "/htmleditor/centeralign.png");
        JButton setRightAlignmentUI = SimpleHTMLEditor.createAlignmentUI(commonComponents, StyleConstants.ALIGN_RIGHT, "align-right", "/htmleditor/rightalign.png");
        JButton setJustifyAlignmentUI = SimpleHTMLEditor.createAlignmentUI(commonComponents, StyleConstants.ALIGN_JUSTIFIED, "align-justify", "/htmleditor/justifyalign.png");
        
        this.toolBar.add(copyUI);
        this.toolBar.add(cutUI);
        this.toolBar.add(pasteContentUI);
        this.toolBar.add(addHyperlinkUI);
        this.toolBar.add(SimpleHTMLEditor.createVerticalSeparator());
        this.toolBar.add(setLeftAlignmentUI);
        this.toolBar.add(setCenterAlignmentUI);
        this.toolBar.add(setRightAlignmentUI);
        this.toolBar.add(setJustifyAlignmentUI);
        this.toolBar.add(SimpleHTMLEditor.createVerticalSeparator());
        this.toolBar.add(makeBoldUI);
        this.toolBar.add(makeItalicUI);
        this.toolBar.add(makeUnderStrikedUI);
        this.toolBar.add(changeForegroundColorUI);
        this.toolBar.add(changeFontSizeUI);
        this.toolBar.add(changeFamilyFontUI);
        
        var componentsRequiringSelection = Arrays.asList(copyUI, cutUI, addHyperlinkUI);
        componentsRequiringSelection.forEach(component -> component.setEnabled(false));
        this.editorPane.addCaretListener(e -> {
            boolean isSelection = e.getDot() != e.getMark();
            componentsRequiringSelection.forEach(component -> component.setEnabled(isSelection));
        });
    }
    
    /**
     * Helper method that sets common parameters to all {@link JButton}s that contain just texts.
     * <p>
     * Currently, it only sets a common Font size to all of them, and said font size is specified in
     * {@link SimpleHTMLEditor#UI_BUTTONS_FONT_SIZE}.
     *
     * @return a new {@link JButton}s with common parameters set.
     */
    private static @NotNull JButton createJButton(String contents) {
        JButton button = new JButton(contents);
        button.setFont(button.getFont().deriveFont(SimpleHTMLEditor.UI_BUTTONS_FONT_SIZE));
        return button;
    }
    
    /**
     * Creates a small vertical separator for separating buttons in a UI that acts like a {@link FlowLayout} with a
     * Horizontal setting.
     *
     * @return a small vertical separator for separating buttons in a UI that acts like a {@link FlowLayout} with a
     *         Horizontal setting.
     */
    private static @NotNull JSeparator createVerticalSeparator() {
        JSeparator separator1 = new JSeparator(SwingConstants.VERTICAL);
        separator1.setMaximumSize(new Dimension(3, 10));
        return separator1;
    }
    
    /**
     * Returns the document as an HTML raw {@link String}.
     *
     * @return the document as an HTML raw {@link String}.
     */
    public String getHTMLContent() {
        return this.editorPane.getText();
    }
    
    /**
     * Sets the focus on the editing foreground area.
     */
    public void focusOnEditor() {
        this.editorPane.requestFocus(false);
    }
    
    /**
     * Returns the minimum {@link Dimension}s this {@link JPanel} should have to show all of its contents.
     *
     * @return the minimum {@link Dimension}s this {@link JPanel} should have to show all of its contents.
     */
    public Dimension minimumDimensions() {
        return new Dimension(this.toolBar.getWidth(), this.toolBar.getHeight() + SimpleHTMLEditor.EDITOR_DIMENSION_MIN_HEIGHT);
    }
    
    /**
     * Common components the UI elements should have access to when triggering actions or when being built.
     */
    private record CommonComponents(JPanel frame, JEditorPane editorPane, HTMLEditorKit htmlEditorKit,
                                    JToolBar toolBar) {
        
        /**
         * Returns the focus to the foreground editor area.
         */
        public void returnFocusToEditor() {
            this.editorPane.requestFocus(false);
        }
    }
    
    /**
     * Creates a {@link JButton} than once pressed, it turns the current selected paragraph(s) to match said alignment.
     *
     * @return a {@link JButton} than once pressed, it turns the current selected paragraph(s) to match said alignment.
     */
    private static JButton createAlignmentUI(CommonComponents commonComponents, int alignment, String actionName, String resourceImage) {
        URL iconResource = Objects.requireNonNull(SimpleHTMLEditor.class.getResource(resourceImage));
        JButton setAlignmentUI = new JButton(ImageLoader.load(iconResource));
        ActionListener alignmentAction = new StyledEditorKit.AlignmentAction(actionName, alignment);
        setAlignmentUI.addActionListener(e -> {
            alignmentAction.actionPerformed(e);
            commonComponents.returnFocusToEditor();
        });
        return setAlignmentUI;
    }
    
    /**
     * Creates a {@link JButton} than once pressed, it shows a dialog for the user to choose a {@link Color}, and if
     * chosen, it sets the current selected foreground to match said {@link Color}.
     *
     * @return a {@link JButton} than once pressed, it shows a dialog for the user to choose a {@link Color}, and if
     *         chosen, it sets the current selected foreground to match said {@link Color}.
     */
    private static JButton createChangeForegroundColorUI(CommonComponents commonComponents) {
        JColorChooser colorChooser = new JColorChooser();
        var foregroundColorButton = new JButton(ImageLoader.load(Objects.requireNonNull(SimpleHTMLEditor.class.getResource("/htmleditor/changecolor.png"))));
        foregroundColorButton.addActionListener(e -> {
            var color = JColorChooser.showDialog(colorChooser, "Choose your foreground color",
                                                 colorChooser.getColor(), false);
            if (color == null) return;
            new StyledEditorKit.ForegroundAction("foreground-color", color).actionPerformed(e);
            commonComponents.returnFocusToEditor();
        });
        return foregroundColorButton;
    }
    
    /**
     * Creates a {@link JComboBox} containing all {@link Font}s the user has installed, and when selecting a letter
     * font, changes the currently selected foreground of the panel to said letter font.
     *
     * @return a {@link JComboBox} containing all {@link Font}s the user has installed, and when selecting a letter
     *         font, changes the currently selected foreground of the panel to said letter font.
     */
    @SuppressWarnings("ZeroLengthArrayAllocation")
    private static JComboBox<String> createChangeFamilyFontUI(CommonComponents commonComponents) {
        var fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        var fontNames = Arrays.stream(fonts).map(Font::getName).toList().toArray(new String[0]);
        JComboBox<String> fontNameBox = new JComboBox<>(fontNames);
        SimpleHTMLEditor.packComboBox(fontNameBox);
        fontNameBox.addActionListener(e -> {
            String requestedFont = Objects.requireNonNull(fontNameBox.getSelectedItem()).toString();
            new StyledEditorKit.FontFamilyAction("font-change", requestedFont).actionPerformed(e);
            commonComponents.returnFocusToEditor();
        });
        return fontNameBox;
    }
    
    /**
     * Creates a {@link JButton} than once pressed, it turns the current selected foreground to bold (Or removes said style if
     * already applied).
     *
     * @return a {@link JButton} than once pressed, it turns the current selected foreground to bold (Or removes said style if
     *         already applied).
     */
    private static JButton createMakeBoldUI(CommonComponents commonComponents) {
        JButton boldButton = SimpleHTMLEditor.createJButton("B");
        ActionListener boldAction = new StyledEditorKit.BoldAction();
        boldButton.setFont(boldButton.getFont().deriveFont(Font.BOLD));
        boldButton.addActionListener(e -> {
            boldAction.actionPerformed(e);
            commonComponents.returnFocusToEditor();
        });
        return boldButton;
    }
    
    /**
     * Creates a {@link JButton} than once pressed, it turns the current selected foreground to italic (Or removes said style
     * if already applied).
     *
     * @return a {@link JButton} than once pressed, it turns the current selected foreground to italic (Or removes said style
     *         if already applied).
     */
    private static JButton createMakeItalicUI(CommonComponents commonComponents) {
        JButton italicButton = SimpleHTMLEditor.createJButton("I");
        italicButton.setFont(italicButton.getFont().deriveFont(Font.ITALIC));
        ActionListener italicAction = new StyledEditorKit.ItalicAction();
        italicButton.addActionListener(e -> {
            italicAction.actionPerformed(e);
            commonComponents.returnFocusToEditor();
        });
        return italicButton;
    }
    
    /**
     * Creates a {@link JButton} than once pressed, it turns the current selected foreground to understrike (Or removes said
     * style if already applied).
     *
     * @return a {@link JButton} than once pressed, it turns the current selected foreground to understrike (Or removes said
     *         style if already applied).
     */
    private static JButton createMakeUnderStrikedUI(CommonComponents commonComponents) {
        JButton understrikeButton = SimpleHTMLEditor.createJButton("U");
        @SuppressWarnings("unchecked")
        Map<TextAttribute, Object> attrs = (Map<TextAttribute, Object>) understrikeButton.getFont().getAttributes();
        attrs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        understrikeButton.setFont(understrikeButton.getFont().deriveFont(attrs));
        ActionListener underlineAction = new StyledEditorKit.UnderlineAction();
        understrikeButton.addActionListener(e -> {
            underlineAction.actionPerformed(e);
            commonComponents.returnFocusToEditor();
        });
        return understrikeButton;
    }
    
    /**
     * Creates a {@link JComboBox} containing letter sizes, and when selecting a letter size, it resizes the current
     * selected foreground of the panel to said letter size.
     *
     * @return a {@link JComboBox} containing letter sizes, and when selecting a letter size, it resizes the current
     * selected foreground of the panel to said letter size.
     */
    @SuppressWarnings({"ZeroLengthArrayAllocation", "DuplicateStringLiteralInspection"})
    private static JComboBox<String> createChangeFontSizeUI(CommonComponents commonComponents) {
        var sizes = SimpleHTMLEditor.FONT_SIZES.stream().map(size -> size + " px.")
                                               .toList();
        JComboBox<String> fontSizeBox = new JComboBox<>(sizes.toArray(new String[0]));
        var defaultFontSizeIndex = SimpleHTMLEditor.FONT_SIZES.indexOf(String.valueOf(SimpleHTMLEditor.DEFAULT_FONT_SIZE_SELECTION));
        if (defaultFontSizeIndex >= 0) {
            fontSizeBox.setSelectedIndex(defaultFontSizeIndex);
        }
        SimpleHTMLEditor.packComboBox(fontSizeBox);
        fontSizeBox.addActionListener(new StyledEditorKit.StyledTextAction("font-resize") {
            @Override public void actionPerformed(ActionEvent e) {
                String fontSizeString = Objects.requireNonNull(fontSizeBox.getSelectedItem()).toString();
                fontSizeString = fontSizeString.substring(0, fontSizeString.indexOf(' '));
                int size = Integer.parseInt(fontSizeString);
                new StyledEditorKit.FontSizeAction("font-resize", size).actionPerformed(e);
                commonComponents.returnFocusToEditor();
            }
        });
        return fontSizeBox;
    }
    
    
    /**
     * Turns a function that gets an element by its index into a {@link Stream} filled with the results of said
     * function.
     * <p>
     * To do so, it requires knowing the count of elements, as it the function returns a {@code null}, it is impossible
     * to know if the getter has reached the end of a collection and is returning null because there are no more
     * elements, or because the current element is actually {@code null}.
     *
     * @return Turns the results of the function into a Stream over these results.
     */
    private static <T> Stream<T> itemGetterToStream(int count, IntFunction<? extends T> give) {
        return IntStream.range(0, count).mapToObj(give);
    }
    
    /**
     * Packs a combo box to the smallest size of its contained elements.
     */
    @SuppressWarnings("MagicNumber")
    private static void packComboBox(JComboBox<?> comboBox) {
        FontMetrics fm = comboBox.getFontMetrics(comboBox.getFont());
        int maxWidth = SimpleHTMLEditor
                .itemGetterToStream(comboBox.getItemCount(), comboBox::getItemAt)
                .map(Object::toString)
                .mapToInt(fm::stringWidth)
                .max()
                .orElse(0);
        comboBox.setMaximumSize(new Dimension(maxWidth + 30, comboBox.getPreferredSize().height));
    }
    
    /**
     * This action opens a dialog to request an URL, and if indicated, it sets said URL as an hyperlink over the
     * currently selected foreground.
     */
    static class AddHyperlinkAction extends StyledEditorKit.StyledTextAction {
        
        /**
         * Default constructor setting the action name as 'hyperlink-add'.
         */
        public AddHyperlinkAction() {
            super("hyperlink-add");
        }
        
        @Override
        public final void actionPerformed(ActionEvent e) {
            JEditorPane editorPane = this.getEditor(e);
            if (editorPane == null) {
                return;
            }
            int start = editorPane.getSelectionStart();
            int end = editorPane.getSelectionEnd();
            if (start == end) {
                JOptionPane.showMessageDialog(editorPane, "No foreground selected.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String selectedText = editorPane.getSelectedText();
            String url = JOptionPane.showInputDialog(editorPane, "Enter the URL for the hyperlink:", "Add Hyperlink", JOptionPane.QUESTION_MESSAGE);
            if (url == null || url.trim().isEmpty()) {
                return;
            }
            String hyperlinkHTML = "<a href=\"" + AddHyperlinkAction.escapeHTML(url.trim()) + "\">" + AddHyperlinkAction.escapeHTML(selectedText) + "</a>";
            try {
                HTMLDocument htmlDocument = (HTMLDocument) editorPane.getDocument();
                HTMLEditorKit htmlEditorKit = (HTMLEditorKit) editorPane.getEditorKit();
                htmlDocument.remove(start, end - start);
                htmlEditorKit.insertHTML(htmlDocument, start, hyperlinkHTML, 0, 0, HTML.Tag.A);
            } catch (BadLocationException | IOException ex) {
                throw new UnrecoverableException(ex);
            }
        }
        
        static final Map<Character, String> ESCAPE_CHARACTERS = Map.of(
                '<', "&lt;",
                '>', "&gt;",
                '&', "&amp;",
                '"', "&quot;"
        );
        
        /**
         * Escapes HTML characters that are used when creating a {@code <a href="*location*"><a/>} element.
         *
         * @return The argument String, but with HTML characters escaped.
         */
        private static String escapeHTML(String text) {
            StringBuilder escapedString = new StringBuilder();
            for (char character : text.toCharArray()) {
                var replacement = Optional.ofNullable(AddHyperlinkAction.ESCAPE_CHARACTERS.get(character));
                escapedString.append(replacement.orElseGet(() -> String.valueOf(character)));
            }
            return escapedString.toString();
        }
    }
    
    /**
     * This class allows users to open hyperlinks.
     * <p>
     * To open a link, the user has to place it's mouse over the hyperlink and to hold the 'Control' key, in which
     * conditions its cursor turns into a {@link Cursor#HAND_CURSOR} and can click to open said hyperlink in their
     * Browser.
     * <p>
     * For this to work, an instance of {@link OpenHyperlinkOnSelectionAction} has to be added as a
     * {@link MouseListener}, a {@link MouseMotionListener} and a {@link KeyListener} on the {@link JEditorPane}.
     */
    @SuppressWarnings("ListenerMayUseAdapter")
    static class OpenHyperlinkOnSelectionAction implements MouseListener, MouseMotionListener, KeyListener {
        
        private static final String HREF_ATTRIBUTE = "href=";
        private static final Cursor HOVERING_CURSOR = new Cursor(Cursor.HAND_CURSOR);
        private static final int KEY_TO_HOLD_FOR_OPENING_URL = KeyEvent.VK_CONTROL;
        
        private final JEditorPane editorPane;
        private boolean holdsKeyForOpeningUrl = false;
        private Point2D mouseLocation = new Point(0, 0);
        private Optional<String> lastTooltipShown = Optional.empty();
        
        /**
         * @param editorPane The panel it keeps track of whether a URL is or not selected to open it.
         */
        OpenHyperlinkOnSelectionAction(final JEditorPane editorPane) {
            this.editorPane = editorPane;
        }
        
        /**
         * Sets itself as a {@link MouseListener}, a {@link MouseMotionListener} and a {@link KeyListener} on the
         * {@link OpenHyperlinkOnSelectionAction#editorPane},
         * allowing to track all the events required by this class.
         */
        public final void setAsListenerOnEditor() {
            this.editorPane.addMouseListener(this);
            this.editorPane.addMouseMotionListener(this);
            this.editorPane.addKeyListener(this);
        }
        
        @Override public void keyTyped(KeyEvent e) {
        }
        
        @Override public void mousePressed(MouseEvent e) {
        }
        
        @Override public void mouseReleased(MouseEvent e) {
        }
        
        @Override public void mouseEntered(MouseEvent e) {
        }
        
        @Override public void mouseDragged(MouseEvent e) {
        }
        
        /**
         * When the window loses focus, it is impossible to know whether the user presses or releases
         * {@link OpenHyperlinkOnSelectionAction#KEY_TO_HOLD_FOR_OPENING_URL}, so it is set as the key wasn't pressed.
         * @param e the event to be processed
         */
        @Override public final void mouseExited(MouseEvent e) {
            this.holdsKeyForOpeningUrl = false;
        }
        
        /**
         * Updates {@link OpenHyperlinkOnSelectionAction#holdsKeyForOpeningUrl} to keep track of when the key for opening
         * the {@link URL} is pressed, setting it to true.
         */
        @Override public final void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == OpenHyperlinkOnSelectionAction.KEY_TO_HOLD_FOR_OPENING_URL) {
                this.holdsKeyForOpeningUrl = true;
            }
            this.onHoveringOnHREF();
        }
        
        /**
         * Updates {@link OpenHyperlinkOnSelectionAction#holdsKeyForOpeningUrl} to keep track of when the key for opening
         * the {@link URL} is released, setting it to false.
         */
        @Override public final void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == OpenHyperlinkOnSelectionAction.KEY_TO_HOLD_FOR_OPENING_URL) {
                this.holdsKeyForOpeningUrl = false;
                //If is showing a tool tip
                if (this.lastTooltipShown.isPresent()){
                    this.editorPane.setCursor(Cursor.getDefaultCursor());
                }
            }
            this.onHoveringOnHREF();
        }
        
        @Override public final void mouseClicked(MouseEvent e) {
            if (!this.holdsKeyForOpeningUrl) {
                return;
            }
            this.getHREFOfSelectedElement().ifPresent(href -> {
                try {
                    Desktop.getDesktop().browse(href);
                } catch (IOException ex) {
                    throw new UnrecoverableException(ex);
                }
            });
            
        }
        
        /**
         * Updates {@link OpenHyperlinkOnSelectionAction#mouseLocation} to keep track of where the mouse is positioned.
         */
        @Override public final void mouseMoved(MouseEvent e) {
            this.mouseLocation = new Point(e.getX(), e.getY());
            if (this.getHREFOfSelectedElement().isPresent()) {
                this.onHoveringOnHREF();
            } else {
                if (this.lastTooltipShown.isPresent() && this.lastTooltipShown.get()
                                                                              .equals(this.editorPane.getToolTipText())) {
                    this.editorPane.setToolTipText(null);
                }
                this.lastTooltipShown = Optional.empty();
            }
        }
        
        /**
         * Takes certain actions when the user is hovering a valid URL:
         * <ul>
         * <li>
         * If holding the {@link OpenHyperlinkOnSelectionAction#KEY_TO_HOLD_FOR_OPENING_URL} key, then the cursor is
         * set to {@link OpenHyperlinkOnSelectionAction#HOVERING_CURSOR}.
         * </li>
         * <li>
         * It sets a tooltip telling the HREF to open.
         * </li>
         * </ul>
         *
         * @see OpenHyperlinkOnSelectionAction#getHREFOfSelectedElement()
         */
        private void onHoveringOnHREF() {
            this.getHREFOfSelectedElement().ifPresent(url -> {
                if (this.holdsKeyForOpeningUrl) {
                    this.editorPane.setCursor(OpenHyperlinkOnSelectionAction.HOVERING_CURSOR);
                }
                String newTooltip = "<html>Control + Click to open:<br>" + url + "</html>";
                String currentTooltip = this.editorPane.getToolTipText();
                if (!newTooltip.equals(currentTooltip)) {
                    this.editorPane.setToolTipText(newTooltip);
                    this.lastTooltipShown=Optional.of(newTooltip);
                }
            });
        }
        
        /**
         * @return If the user has holds the 'Control' key and its mouse is over an element with a 'href' pointing to a
         *         valid {@link URL}, it returns said {@link URL} as a {@link URI}, in any other case, it returns
         *         {@link Optional#empty()}.
         */
        private Optional<URI> getHREFOfSelectedElement() {
            if (!(this.editorPane.getDocument() instanceof StyledDocument doc)) {
                return Optional.empty();
            }
            int pos = this.editorPane.viewToModel2D(this.mouseLocation);
            Element element = doc.getCharacterElement(pos);
            AttributeSet attributes = element.getAttributes();
            Object attributeA = attributes.getAttribute(HTML.Tag.A);
            Object attributeHREF = attributes.getAttribute(HTML.Tag.HTML);
            Object attributeWithLink = Optional.ofNullable(attributeA).orElse(attributeHREF);
            if (attributeWithLink == null) {
                return Optional.empty();
            }
            String href = attributeWithLink.toString();
            if (href.startsWith(OpenHyperlinkOnSelectionAction.HREF_ATTRIBUTE)) {
                href = href.substring(OpenHyperlinkOnSelectionAction.HREF_ATTRIBUTE.length());
            }
            try {
                return Optional.of(new URL(href).toURI());
            } catch (URISyntaxException | MalformedURLException e) {
                return Optional.empty();
            }
        }
        
    }
    
}