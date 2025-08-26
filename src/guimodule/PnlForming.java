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
 * Main Swing container that orchestrates theme, list and form panels using
 * pure delegation and lambdas. No direct controller/UI wiring.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class PnlForming extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BusinesslogicaDelegation delegate;
    private final GuiModuleDelegate modules;

    @Deprecated
    public PnlForming(String title) {
        super();
        this.modules = GuiModuleDelegate.createDefault();
        this.delegate = modules.business();
        init();
    }

    @Deprecated
    public PnlForming(String title, BusinesslogicaDelegation delegate) {
        super();
        this.modules = GuiModuleDelegate.createDefault();
        this.delegate = delegate;
        init();
    }

    public PnlForming(BusinesslogicaDelegation delegate) {
        super();
        this.modules = GuiModuleDelegate.createDefault();
        this.delegate = delegate;
        init();
    }

    public PnlForming(GuiModuleDelegate modules) {
        super();
        this.modules = modules;
        this.delegate = modules.business();
        init();
    }

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

        // Build panels
        Theme themePanel = new Theme(modules);
        CreateQuizQuestionListPanel listPanel = new CreateQuizQuestionListPanel(modules);

        // Mutable holder to reference form after creation inside lambdas
        final CreateQuizQuestionsPanel[] formRef = new CreateQuizQuestionsPanel[1];

        CreateQuizQuestionsPanel formPanel = new CreateQuizQuestionsPanel(
            delegate,
            listPanel::getSelectedTopic,
            listPanel::refreshListForSelectedTopic,
            () -> { listPanel.refreshListForSelectedTopic(); formRef[0].clearForm(); },
            () -> listPanel.getFragenListe().getSelectedIndex()
        );
        formRef[0] = formPanel;

        // Create Quiz Game Panel via modules
        ModularQuizPlay quizGamePanel = new ModularQuizPlay(modules);

        // Create Statistics Panel via modules
        ModularStatisticsPanel statisticsPanel = new ModularStatisticsPanel(modules);

        // Wire quiz game to statistics panel for data transfer
        quizGamePanel.setStatisticsService(statisticsPanel);

        // Wire list panel callbacks
        listPanel.setOnNewQuestion(formPanel::clearForm);
        listPanel.setOnQuestionSelected((topic, idx) -> {
            RepoQuizeeQuestions r = delegate.getQuestion(topic, idx);
            if (r == null) { formPanel.clearForm(); return; }
            QuizQuestion q = QuizDataMapper.toQuizQuestion(r);
            formPanel.setFormData(q);
        });

        // Wire theme panel to update question list and statistics when topics change
        themePanel.setOnTopicChanged(() -> {
            listPanel.reloadTopics();
            listPanel.refreshListForSelectedTopic();
            quizGamePanel.loadThemes(); // Update quiz game theme dropdown
            statisticsPanel.refreshThemesAndStatistics(); // Update statistics theme dropdown
        });

        // Tabs: Theme + Questions (list+form side-by-side)
        JPanel questionsComposite = new JPanel(new GridLayout(1, 2));
        questionsComposite.add(formPanel);
        questionsComposite.add(listPanel);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📝 Quiz Topics", themePanel);
        tabs.addTab("❓ Quiz Questions", questionsComposite);
        tabs.addTab("🎮 Quiz Game", quizGamePanel);
        tabs.addTab("📊 Statistics", statisticsPanel);

        add(tabs, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1000, 700));
    }

}
