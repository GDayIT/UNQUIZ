package guimodule;

import dbbl.BusinesslogicaDelegation;
import dbbl.RepoQuizeeQuestions;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.Serializable;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

/**
 * PnlForming serves as the main container for the modular quiz application.
 * <p>
 * It orchestrates all panels including:
 * <ul>
 *     <li>Theme management panel</li>
 *     <li>Quiz question list panel</li>
 *     <li>Quiz question form panel</li>
 *     <li>Quiz game panel</li>
 *     <li>Statistics panel</li>
 * </ul>
 * <p>
 * All interactions are implemented via delegation and lambda callbacks,
 * ensuring separation of concerns and minimizing direct controller/UI coupling.
 * This panel also applies Nimbus Look and Feel if available.
 * <p>
 * Supports deprecated constructors for backward compatibility but recommends
 * using delegates or module delegates for fully modular operation.
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Panel initialization and layout management</li>
 *     <li>Delegating data access and actions to BusinesslogicaDelegation</li>
 *     <li>Wiring quiz game and statistics panel for real-time updates</li>
 *     <li>Coordinating updates between Theme, List, and Form panels</li>
 *     <li>Providing tabbed interface for user navigation</li>
 * </ul>
 * <p>
 * This class implements Serializable to allow persistence of UI state if needed.
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 */
public class PnlForming extends JPanel implements Serializable {

    /** Serialization version identifier */
    private static final long serialVersionUID = 1L;

    /**
     * Delegate for business logic access.
     * Provides methods to retrieve, store, and manipulate quiz questions and themes.
     */
    private final BusinesslogicaDelegation delegate;

    /**
     * Module delegate providing centralized access to application modules.
     * Ensures cohesive instantiation of subpanels and services.
     */
    private final GuiModuleDelegate modules;

    /**
     * Deprecated constructor for backward compatibility.
     * Uses default module delegate and retrieves its business delegate.
     * @param title Arbitrary title placeholder (ignored internally)
     */
    @Deprecated
    public PnlForming(String title) {
        super();
        this.modules = GuiModuleDelegate.createDefault();
        this.delegate = modules.business();
        init();
    }

    /**
     * Deprecated constructor for backward compatibility allowing custom delegate.
     * @param title Arbitrary title placeholder (ignored internally)
     * @param delegate Business logic delegate to use
     */
    @Deprecated
    public PnlForming(String title, BusinesslogicaDelegation delegate) {
        super();
        this.modules = GuiModuleDelegate.createDefault();
        this.delegate = delegate;
        init();
    }

    /**
     * Preferred constructor using a specific business logic delegate.
     * @param delegate Business logic delegate to handle data operations
     */
    public PnlForming(BusinesslogicaDelegation delegate) {
        super();
        this.modules = GuiModuleDelegate.createDefault();
        this.delegate = delegate;
        init();
    }

    /**
     * Preferred constructor using a full GuiModuleDelegate.
     * @param modules Complete module delegate providing access to business and submodules
     */
    public PnlForming(GuiModuleDelegate modules) {
        super();
        this.modules = modules;
        this.delegate = modules.business();
        init();
    }

    /**
     * Initializes all subpanels and sets up layout and tabbed interface.
     * <p>
     * Responsibilities include:
     * <ul>
     *     <li>Setting BorderLayout for the main panel</li>
     *     <li>Applying Nimbus Look and Feel if available</li>
     *     <li>Instantiating and wiring all quiz-related panels:
     *         Theme, List, Form, Game, Statistics</li>
     *     <li>Wiring lambda callbacks for form clearing, question selection,
     *         and statistics updates</li>
     *     <li>Arranging panels into tabs with icons for UX clarity</li>
     * </ul>
     */
    private void init() {
        setLayout(new BorderLayout());

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        // Build Theme panel for selecting active quiz topics
        Theme themePanel = new Theme(modules);

        // Build question list panel
        CreateQuizQuestionListPanel listPanel = new CreateQuizQuestionListPanel(modules);

        // Mutable reference for form panel to use in lambdas
        final CreateQuizQuestionsPanel[] formRef = new CreateQuizQuestionsPanel[1];

        // Build question form panel with delegated callbacks
        CreateQuizQuestionsPanel formPanel = new CreateQuizQuestionsPanel(
            delegate,
            listPanel::getSelectedTopic,
            listPanel::refreshListForSelectedTopic,
            () -> { listPanel.refreshListForSelectedTopic(); formRef[0].clearForm(); },
            () -> listPanel.getFragenListe().getSelectedIndex()
        );
        formRef[0] = formPanel;

        // Create modular quiz game panel
        ModularQuizPlay quizGamePanel = new ModularQuizPlay(modules);

        // Create modular statistics panel
        ModularStatisticsPanel statisticsPanel = new ModularStatisticsPanel(modules);

        // Connect quiz game to statistics panel for real-time updates
        quizGamePanel.setStatisticsService(statisticsPanel);

        // Setup list panel callbacks for form updates
        listPanel.setOnNewQuestion(formPanel::clearForm);
        listPanel.setOnQuestionSelected((topic, idx) -> {
            RepoQuizeeQuestions r = delegate.getQuestion(topic, idx);
            if (r == null) { formPanel.clearForm(); return; }
            QuizQuestion q = QuizDataMapper.toQuizQuestion(r);
            formPanel.setFormData(q);
        });

        // Setup theme panel callbacks to refresh related panels when topics change
        themePanel.setOnTopicChanged(() -> {
            listPanel.reloadTopics();
            listPanel.refreshListForSelectedTopic();
            quizGamePanel.loadThemes();
            statisticsPanel.refreshThemesAndStatistics();
        });

        // Composite panel for side-by-side form and list
        JPanel questionsComposite = new JPanel(new GridLayout(1, 2));
        questionsComposite.add(formPanel);
        questionsComposite.add(listPanel);

        // Tabbed pane to navigate between Theme, Questions, Game, and Statistics
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📝 Quiz Topics", themePanel);
        tabs.addTab("❓ Quiz Questions", questionsComposite);
        tabs.addTab("🎮 Quiz Game", quizGamePanel);
        tabs.addTab("📊 Statistics", statisticsPanel);

        add(tabs, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1000, 700));
    }
}