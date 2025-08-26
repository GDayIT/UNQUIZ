package guimodule;

import java.util.List;
import java.util.function.*;

/**
 * Functional delegation interface for GUI operations.
 * 
 * This interface provides lambda-based contracts for all GUI interactions,
 * enabling complete separation between GUI components and business logic.
 * 
 * Key principles:
 * - No direct GUI component references in business logic
 * - All interactions through functional interfaces
 * - Support for reactive programming patterns
 * - Event-driven architecture with callbacks
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
=======
 * @author Quiz Application Team
 * @version 2.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public interface GuiDelegate {
    
    // === FORM OPERATIONS ===
    
    /**
     * Lambda for form data collection.
     * Supplier returns current form data.
     */
    Supplier<FormData> collectFormData();
    
    /**
     * Lambda for form population.
     * Consumer receives data to populate form with.
     */
    Consumer<FormData> populateForm();
    
    /**
     * Lambda for form validation.
     * Function takes form data and returns validation result.
     */
    Function<FormData, ValidationResult> validateForm();
    
    /**
     * Lambda for form clearing.
     * Runnable clears all form fields.
     */
    Runnable clearForm();
    
    // === MESSAGE OPERATIONS ===
    
    /**
     * Lambda for displaying messages.
     * Consumer receives message data.
     */
    Consumer<MessageData> showMessage();
    
    /**
     * Lambda for confirmation dialogs.
     * Function takes message and returns user choice.
     */
    Function<String, Boolean> showConfirmation();
    
    /**
     * Lambda for error display.
     * Consumer receives error information.
     */
    Consumer<ErrorData> showError();
    
    // === LIST OPERATIONS ===
    
    /**
     * Lambda for list updates.
     * Consumer receives new list data.
     */
    Consumer<List<String>> updateList();
    
    /**
     * Lambda for list selection.
     * Supplier returns currently selected item.
     */
    Supplier<String> getSelectedItem();
    
    /**
     * Lambda for list selection changes.
     * Consumer receives selection change events.
     */
    Consumer<SelectionEvent> onSelectionChanged();
    
    // === BUTTON OPERATIONS ===
    
    /**
     * Lambda for button actions.
     * Runnable executes when button is clicked.
     */
    Runnable onButtonClick();
    
    /**
     * Lambda for button state changes.
     * Consumer receives button state data.
     */
    Consumer<ButtonState> setButtonState();
    
    // === NAVIGATION OPERATIONS ===
    
    /**
     * Lambda for tab switching.
     * Consumer receives target tab identifier.
     */
    Consumer<String> switchToTab();
    
    /**
     * Lambda for panel updates.
     * Consumer receives panel update data.
     */
    Consumer<PanelUpdateData> updatePanel();
    
    // === DATA TRANSFER OBJECTS ===
    
    /**
     * Generic form data container.
     */
    class FormData {
        public final String title;
        public final String content;
        public final List<String> options;
        public final List<Boolean> flags;
        public final long timestamp;
        
        public FormData(String title, String content, List<String> options, List<Boolean> flags) {
            this.title = title;
            this.content = content;
            this.options = options;
            this.flags = flags;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Validation result container.
     */
    class ValidationResult {
        public final boolean isValid;
        public final List<String> errors;
        public final String summary;
        
        public ValidationResult(boolean isValid, List<String> errors, String summary) {
            this.isValid = isValid;
            this.errors = errors;
            this.summary = summary;
        }
    }
    
    /**
     * Message display data.
     */
    class MessageData {
        public final String text;
        public final MessageType type;
        public final int duration; // milliseconds
        
        public MessageData(String text, MessageType type, int duration) {
            this.text = text;
            this.type = type;
            this.duration = duration;
        }
        
        public enum MessageType {
            INFO, SUCCESS, WARNING, ERROR
        }
    }
    
    /**
     * Error display data.
     */
    class ErrorData {
        public final String title;
        public final String message;
        public final Throwable cause;
        public final boolean showDetails;
        
        public ErrorData(String title, String message, Throwable cause, boolean showDetails) {
            this.title = title;
            this.message = message;
            this.cause = cause;
            this.showDetails = showDetails;
        }
    }
    
    /**
     * Selection change event.
     */
    class SelectionEvent {
        public final String previousSelection;
        public final String currentSelection;
        public final int index;
        public final long timestamp;
        
        public SelectionEvent(String previousSelection, String currentSelection, int index) {
            this.previousSelection = previousSelection;
            this.currentSelection = currentSelection;
            this.index = index;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Button state data.
     */
    class ButtonState {
        public final boolean enabled;
        public final String text;
        public final String tooltip;
        
        public ButtonState(boolean enabled, String text, String tooltip) {
            this.enabled = enabled;
            this.text = text;
            this.tooltip = tooltip;
        }
    }
    
    /**
     * Panel update data.
     */
    class PanelUpdateData {
        public final String panelId;
        public final Object data;
        public final UpdateType type;
        
        public PanelUpdateData(String panelId, Object data, UpdateType type) {
            this.panelId = panelId;
            this.data = data;
            this.type = type;
        }
        
        public enum UpdateType {
            REFRESH, CLEAR, POPULATE, VALIDATE
        }
    }
}
