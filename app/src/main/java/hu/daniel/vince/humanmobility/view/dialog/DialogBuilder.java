package hu.daniel.vince.humanmobility.view.dialog;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import hu.daniel.vince.humanmobility.R;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 02. 20.
 */

public class DialogBuilder {

    public static AlertDialog createSimpleMessageDialog(
            Context context,
            String title,
            String message,
            String negativeButton,
            final DialogInterface.OnClickListener negativeButtonListener,
            String positiveButton,
            final DialogInterface.OnClickListener positiveButtonListener) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.AlertDialogStyle);

        if (title != null) {
            builder.setTitle(title);
        }

        if (message != null) {
            final ViewGroup viewGroup =
                    (ViewGroup) View.inflate(context, R.layout.dialog_message, null);
            final TextView messageTextView = (TextView) viewGroup.findViewById(R.id.message_text);
            messageTextView.setText(message);
            builder.setView(viewGroup);
        }

        if (negativeButton != null) {
            builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (negativeButtonListener != null) {
                        negativeButtonListener.onClick(dialog, which);
                    }
                }
            });
        }

        if (positiveButton != null) {
            builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (positiveButtonListener != null) {
                        positiveButtonListener.onClick(dialog, which);
                    }
                }
            });
        }

        return builder.create();
    }


    public static AlertDialog createErrorDialog(
            Context context,
            @StringRes int messageRes,
            final DialogInterface.OnClickListener positiveButtonListener) {
        return createErrorDialog(context,
                context.getResources().getString(messageRes),
                positiveButtonListener);
    }

    public static AlertDialog createErrorDialog(
            Context context,
            String message,
            final DialogInterface.OnClickListener positiveButtonListener) {
        return createSimpleMessageDialog(
                context, context.getResources().getString(R.string.application_error), message, null, null, context.getResources().getString(R.string.application_ok), positiveButtonListener);
    }

    public static AlertDialog createLoadingDialog(Context context) {
        final ViewGroup viewGroup =
                (ViewGroup) View.inflate(context, R.layout.dialog_loading, null);

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setView(viewGroup);
        builder.setCancelable(false);

        return builder.create();
    }

    public static AlertDialog createQuestionDialog(
            Context context,
            @StringRes int messageRes,
            @StringRes int positiveButtonRes,
            final DialogInterface.OnClickListener positiveButtonListener) {
        return createQuestionDialog(
                context,
                context.getResources().getString(messageRes),
                context.getResources().getString(positiveButtonRes),
                positiveButtonListener);
    }

    public static AlertDialog createQuestionDialog(
            Context context,
            String message,
            String positiveButton,
            final DialogInterface.OnClickListener positiveButtonListener) {
        return createSimpleMessageDialog(
                context,
                context.getResources().getString(R.string.application_warning),
                message,
                context.getResources().getString(R.string.application_cancel),
                null,
                positiveButton,
                positiveButtonListener);
    }

    public static AlertDialog createSingleChoiceDialog(
            Context context,
            @StringRes int titleRes,
            @ArrayRes int arrayRes,
            int current,
            final DialogInterface.OnClickListener onCancelListener,
            final DialogInterface.OnClickListener onSelectListener) {

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.AlertDialogStyle);

        builder.setTitle(titleRes);
        builder.setPositiveButton(R.string.application_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (onCancelListener != null) {
                    onCancelListener.onClick(dialog, which);
                }
            }
        });

        builder.setSingleChoiceItems(arrayRes, current, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onSelectListener.onClick(dialog, which);
            }
        });

        return builder.create();
    }

    public static AlertDialog createWebViewDialog(
            Context context,
            String title,
            String url,
            boolean isInFile) {

        final ViewGroup viewGroup =
                (ViewGroup) View.inflate(context, R.layout.dialog_webview, null);
        final WebView webView = (WebView) viewGroup.findViewById(R.id.dialog_webView);

        if(isInFile)
            webView.loadUrl("file:///android_asset/" + url + ".html");
        else
            webView.loadUrl(url);

        webView.clearCache(true);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setOnLongClickListener(v -> true);

        final LayoutTransition transition = new LayoutTransition();
        transition.setDuration(500);
        webView.setLayoutTransition(transition);


        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.AlertDialogStyle);

        builder.setTitle(title);
        builder.setView(viewGroup);

        builder.setPositiveButton(R.string.application_close, (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}
