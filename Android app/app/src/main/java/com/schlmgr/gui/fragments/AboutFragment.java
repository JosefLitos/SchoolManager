package com.schlmgr.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.schlmgr.R;
import com.schlmgr.gui.Controller;
import com.schlmgr.gui.Controller.ControlListener;

public class AboutFragment extends Fragment implements ControlListener {

	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_about, container, false);
		((TextView) root.findViewById(R.id.about)).setText("Search syntax:" +
				"\n\tcontains: no prefix or \\\\\n\tregex: \\r\n\tstarts with:\\s\n\tends with: \\e\n" +
				"Except regex search, naming syntax is allowed and parsed!\n\n" +
				"Regex Source: https://www.javatpoint.com/java-regex\n\n" +
				"Regex Character classes\n" +
				"Char Class\t\t│ Description\n" +
				"[abc]\t\t\t\t\t│ a, b, or c (simple class)\n" +
				"[^abc]\t\t\t\t│ Any character except a, b, or c (negation)\n" +
				"[a-zA-Z]\t\t\t│ a through z or A through Z, inclusive (range)\n" +
				"[a-d[m-p]]\t\t│ a through d, or m through p: [a-dm-p] (union)\n" +
				"[a-z&&[def]]\t│ d, e, or f (intersection)\n" +
				"[a-z&&[^bc]]\t│ a through z, except for b and c: [ad-z] (subtraction)\n" +
				"[a-z&&[^m-p]]\t│ a through z, and not m through p: [a-lq-z](subtraction)\n\n" +
				"Regex Quantifiers\n" +
				"The quantifiers specify the number of occurrences of a character.\n" +
				"Regex\t │ Description\n" +
				"X?\t\t\t │ X occurs once or not at all\n" +
				"X+\t\t\t │ X occurs once or more times\n" +
				"X*\t\t\t │ X occurs zero or more times\n" +
				"X{n}\t\t │ X occurs n times only\n" +
				"X{n,}\t\t │ X occurs n or more times\n" +
				"X{y,z}\t │ X occurs at least y times but less than z times\n\n" +
				"Regex Metacharacters\n" +
				"The regular expression metacharacters work as shortcodes.\n" +
				"Regex\t │ Description\n" +
				".\t\t\t\t │ Any character (may or may not match terminator)\n" +
				"\\d\t\t\t │ Any digits, short of [0-9]\n" +
				"\\D\t\t\t │ Any non-digit, short for [^0-9]\n" +
				"\\s\t\t\t │ Any whitespace character, short for [\\t\\n\\x0B\\f\\r]\n" +
				"\\S\t\t\t │ Any non-whitespace character, short for [^\\s]\n" +
				"\\w\t\t\t │ Any word character, short for [a-zA-Z_0-9]\n" +
				"\\W\t\t\t │ Any non-word character, short for [^\\w]\n" +
				"\\b\t\t\t │ A word boundary\n" +
				"\\B\t\t\t │ A non word boundary\n\n" +
				"Versions:\n" +
				"\tBeta 7.5:\n\t\t- implemented menu after selecting\n\t\t- added delete function\n\t\t- added reference function\n\t\t- added cut function\n\t\t- added edit function\n" +
				"\tBeta 7.0:\n\t\t- new picture function implemented\n\t\t- new word function adapted for multiple translates\n\t\t- fixed popup stays on screen\n" +
				"\tBeta 6.6:\n\t\t- rapidly improved startup time\n\t\t- restructured code to use more inheritance\n\t\t- improved library's platform independency\n\t\t- added choose dir options\n\t\t- added external storage workflow\n\t\t- fixed permission issues\n\t\t- fix: app crash after removed dir\n" +
				"\tBeta 6.3:\n\t\t- added settings functions\n\t\t- corrected design\n\t\t- added interface options\n" +
				"\tBeta 6.2:\n\t\t- added showing parsed names (set as default behaviour)\n\t\t- added more sorting methods\n\t\t- fix: search not parsing searched text\n" +
				"\tBeta 6.1:\n\t\t- fix: translate description not saving\n\t\t- fix: word creation not creating last word\n" +
				"\tBeta 6.0:\n\t\t- added 'New Object/Chapter/Word' option\n\t\t- fix: autosave after change\n" +
				"\tBeta 5.3:\n\t\t- added 'Change source folder' option\n\t\t- fix: autosaving not working (data loss)\n" +
				"\tBeta 5.1:\n\t\t- remake: Searching interface & GUI, Search field auto-hide\n\t\t- added search syntax variants\n\t\t- fix: Reference change search path (app crash)\n" +
				"\tBeta 5.0:\n\t\t- added Search field, IOException handlers\n\t\t- fix: database data loss, fix: not saving added objects\n" +
				"\tBeta 4.1:\n\t\t- added App Icon, remake: Sorting interface\n" +
				"\tBeta 4.0:\n\t\t- added Sorting options (alphabet + success rate)\n\t\t- fix: Reference bugs\n" +
				"\tBeta 3.1:\n\t\t- added Word hierarchy import, Word description screen\n" +
				"\tBeta 3.0:\n\t\t- added 'more' menu, Word hierarchy export\n" +
				"\tBeta 2.1:\n\t\t- improved Exception handling, added copy button\n\t\t- remake: app menu design\n" +
				"\tBeta 2.0:\n\t\t- added Image displaying, Exception informer\n\t\t- fix: Reference bugs\n" +
				"\tBeta 1.2:\n\t\t- added Reference support, AutoSave changes\n\t\t- fix: large description\n" +
				"\tBeta 1.1:\n\t\t- added icons and success color indication,\n\t\t- added Word translation toggle, multiple description support\n" +
				"\tBeta 1.0:\n\t\tAble to show content of compiled hierarchies.");
		return root;
	}

	@Override
	public void onResume() {
		Controller.setCurrentControl(this,0, false);
		super.onResume();
	}

	@Override
	public void run() {
		Controller.defaultBack.run();
	}
}