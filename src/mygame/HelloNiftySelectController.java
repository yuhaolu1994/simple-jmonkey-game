package mygame;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.List;

public class HelloNiftySelectController implements ScreenController {

    private final Assignment5 app;
    private List<String> selection;
    public HelloNiftySelectController(Assignment5 app) {
        this.app = app;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        ListBox theBox = screen.findNiftyControl("selectionBox", ListBox.class);
        for (String color : app.colorSelections.keySet()) {
            theBox.addItem(color);
        }
        for (String model : app.modelSelections) {
            theBox.addItem(model);
        }
        for (String ballNumber : app.ballSelections) {
            theBox.addItem(ballNumber);
        }
    }

    @NiftyEventSubscriber(id = "selectionBox")
    public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
        selection = event.getSelection();       
    }

    @NiftyEventSubscriber(id = "doneButton")
    public void onDoneButtonClicked(final String id, final ButtonClickedEvent event) {
        app.doneSelecting();
        app.colorSelected(selection.get(0));
        app.modelSelected(selection.get(1));
        app.ballSelected(selection.get(2));
    }
    
    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
}
