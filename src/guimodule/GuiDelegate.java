package guimodule;

import java.util.List;
import java.util.function.*;

/**
 * Functional delegation interface for GUI operations.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Provides lambda-based contracts for all GUI interactions.</li>
 *   <li>Ensures complete separation between GUI components and business logic.</li>
 *   <li>Supports reactive programming and event-driven patterns via callbacks.</li>
 *   <li>Encapsulates CRUD operations for Forms, Lists, Buttons, and Panels.</li>
 *   <li>Designed for Themes, Questions, Leitner Cards, and Session management.</li>
 * </ul>
 * <p>
 * Key Principles:
 * <ul>
 *   <li>No direct GUI component references in business logic.</li>
 *   <li>All interactions via functional interfaces (Supplier, Consumer, Function, Runnable).</li>
 *   <li>Fully compatible with modular, lambda-driven orchestration.</li>
 * </ul>
 * 
 * @author D.
 * @version 1.0
 */
public interface GuiDelegate {

    // === FORM OPERATIONS ===

    /** Supplier returning current form data. */
    Supplier<FormData> collectFormData();

    /** Consumer populating form with given data. */
    Consumer<FormData> populateForm();

    /** Function validating the form and returning a ValidationResult. */
    Function<FormData, ValidationResult> validateForm();

    /** Runnable that clears all form fields. */
    Runnable clearForm();

    // === MESSAGE OPERATIONS ===

    /** Consumer that displays messages with MessageData. */
    Consumer<MessageData> showMessage();

    /** Function that shows a confirmation dialog and returns user choice. */
    Function<String, Boolean> showConfirmation();

    /** Consumer that displays errors with ErrorData. */
    Consumer<ErrorData> showError();

    // === LIST OPERATIONS ===

    /** Consumer that updates a list with new data. */
    Consumer<List<String>> updateList();

    /** Supplier returning currently selected item in a list. */
    Supplier<String> getSelectedItem();

    /** Consumer that receives list selection change events. */
    Consumer<SelectionEvent> onSelectionChanged();

    // === BUTTON OPERATIONS ===

    /** Runnable invoked when a button is clicked. */
    Runnable onButtonClick();

    /** Consumer to update button state (enabled, text, tooltip). */
    Consumer<ButtonState> setButtonState();

    // === NAVIGATION OPERATIONS ===

    /** Consumer to switch to a different tab. */
    Consumer<String> switchToTab();

    /** Consumer to update a panel with data. */
    Consumer<PanelUpdateData> updatePanel();

    // === DATA TRANSFER OBJECTS ===

    /** Form data container used for Questions, Cards, and Sessions. */
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

    /** Validation result container with status and error messages. */
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

    /** Message display container for info, success, warning, or error messages. */
    class MessageData {
        public final String text;
        public final MessageType type;
        public final int duration; // milliseconds

        public MessageData(String text, MessageType type, int duration) {
            this.text = text;
            this.type = type;
            this.duration = duration;
        }

        public enum MessageType { INFO, SUCCESS, WARNING, ERROR }
    }

    /** Error display container with optional throwable and detail toggle. */
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

    /** List selection change event container. */
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

    /** Button state container (enabled status, text, tooltip). */
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

    /** Panel update container for refresh, clear, populate, or validate operations. */
    class PanelUpdateData {
        public final String panelId;
        public final Object data;
        public final UpdateType type;

        public PanelUpdateData(String panelId, Object data, UpdateType type) {
            this.panelId = panelId;
            this.data = data;
            this.type = type;
        }

        public enum UpdateType { REFRESH, CLEAR, POPULATE, VALIDATE }
    }
}