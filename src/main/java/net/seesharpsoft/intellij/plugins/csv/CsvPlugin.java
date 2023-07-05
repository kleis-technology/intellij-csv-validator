package net.seesharpsoft.intellij.plugins.csv;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.*;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import net.seesharpsoft.intellij.plugins.csv.components.CsvFileAttributes;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettings;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettingsProvider;
import org.jetbrains.annotations.NotNull;

public class CsvPlugin implements StartupActivity, StartupActivity.DumbAware, StartupActivity.Background {

    protected static IdeaPluginDescriptor getPluginDescriptor() {
        return PluginManagerCore.getPlugin(PluginId.getId("net.seesharpsoft.intellij.plugins.csv"));
    }

    protected static String getVersion() {
        return getPluginDescriptor().getVersion();
    }

    protected static String getChangeNotes() {
        return getPluginDescriptor().getChangeNotes();
    }

    private static void openLink(Project project, String link) {
        if (project.isDisposed()) return;

        if (link.startsWith("#")) {
            ((ShowSettingsUtilImpl) ShowSettingsUtil.getInstance()).showSettingsDialog(project, link.substring(1), null);
        } else {
            BrowserUtil.browse(link, project);
        }
    }

    public static void doAsyncProjectMaintenance(@NotNull Project project) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "CSV Editor validation") {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // initialize progress indication
                progressIndicator.setIndeterminate(false);

                // Set the progress bar percentage and text
                progressIndicator.setFraction(0.50);
                progressIndicator.setText("Validating CSV file attributes");

                // start process
                try {
                    CsvFileAttributes csvFileAttributes = CsvFileAttributes.getInstance(getProject());
                    csvFileAttributes.cleanupAttributeMap(getProject());
                } catch (Exception exception) {
                    // repeated unresolved bug-reports when retrieving the component
                    // while this cleanup is an optional and non-critical task
                }
                // finished
                progressIndicator.setFraction(1.0);
                progressIndicator.setText("finished");
            }
        });
    }

    @Override
    public void runActivity(@NotNull Project project) {
        doAsyncProjectMaintenance(project);
    }
}