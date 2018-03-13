package hu.daniel.vince.humanmobility.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.handlers.connection.ConnectionHandler;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.BugReport;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-27.
 */

public class BugReportFragment extends Fragment {

    // region Members

    Button okButton;
    LinearLayout linearLayout;

    // endregion

    // region Instance

    public static BugReportFragment newInstance() {
        return new BugReportFragment();
    }

    // endregion

    // region Overrides

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        linearLayout =
                (LinearLayout) inflater.inflate(R.layout.fragment_bugreport, null);

        okButton = (Button)  linearLayout.findViewById(R.id.sendBugReport);

        setOnClickListeners();
        return linearLayout;

    }

    // endregion

    // region Helpers

    public void setOnClickListeners() {
        okButton.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }

            EditText edittext = (EditText) linearLayout.findViewById(R.id.bugReport_input);
            String message = edittext.getText().toString();

            if(message.length() < 6) {
                Toast.makeText(
                        getContext(),
                        R.string.bug_report_short_message, Toast.LENGTH_SHORT).show();
                return;
            }

            BugReport report = new BugReport(message);
            ApplicationUser user = DatabaseHandler.getInstance(getContext()).getUser();

            ConnectionHandler.getInstance(getContext()).getReportManager()
                    .sendBugReport(user, report);

            edittext.setText("");

            Toast.makeText(
                    getContext(),
                    R.string.bug_report_succ_info, Toast.LENGTH_SHORT).show();
        });
    }

    // endregion
}
