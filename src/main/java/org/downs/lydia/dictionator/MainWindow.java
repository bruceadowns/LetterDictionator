package org.downs.lydia.dictionator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.text.SimpleDateFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.MessageBox;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainWindow {

	static void makeCombinations(String sCurrent, int iRecurseCounter,
			String sLetters, HashSet<String> hsCombos, boolean[] bUsed) {
		if (iRecurseCounter == 0) {
			hsCombos.add(sCurrent);
		} else {
			--iRecurseCounter;
			for (int i = 0; i < sLetters.length(); ++i) {
				if (!bUsed[i]) {
					bUsed[i] = true;
					makeCombinations(sCurrent + sLetters.charAt(i),
							iRecurseCounter, sLetters, hsCombos, bUsed);
					bUsed[i] = false;
				}
			}
		}
	}

	public static void main(String[] args) {

		Display display = new Display();
		final Shell shell = new Shell(display);

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = 10;
		formLayout.marginHeight = 10;
		formLayout.spacing = 10;
		shell.setLayout(formLayout);

		/*
		 * controls
		 */
		Label labelTitle = new Label(shell, SWT.NONE);
		labelTitle.setText("Lydia's Letter Dictionator");
		Font initialFont = labelTitle.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(24);
		}
		Font newFont = new Font(display, fontData);
		labelTitle.setFont(newFont);

		Label labelMin = new Label(shell, SWT.NONE);
		labelMin.setText("Min:");
		final Text textMin = new Text(shell, SWT.BORDER);
		textMin.setText("1");
		textMin.addVerifyListener(new VerifyListener() {
			public void verifyText(final VerifyEvent event) {
				switch (event.keyCode) {
				case SWT.BS:
				case SWT.DEL:
				case SWT.HOME:
				case SWT.END:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_RIGHT:
					return;
				}
				if (!Character.isDigit(event.character)) {
					event.doit = false;
				}
			}
		});
		Label labelMax = new Label(shell, SWT.NONE);
		labelMax.setText("Max:");
		final Text textMax = new Text(shell, SWT.BORDER);
		textMax.setText("10");
		textMax.addVerifyListener(new VerifyListener() {
			public void verifyText(final VerifyEvent event) {
				switch (event.keyCode) {
				case SWT.BS:
				case SWT.DEL:
				case SWT.HOME:
				case SWT.END:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_RIGHT:
					return;
				}
				if (!Character.isDigit(event.character)) {
					event.doit = false;
				}
			}
		});

		Label labelLetters = new Label(shell, SWT.NONE);
		labelLetters.setText("Letters:");
		final Text textLetters = new Text(shell, SWT.BORDER);
		// textLetters.setText("abcdefghijklmnopqrstuvwxyz");
		textLetters.setText("aiichhnmcs");
		textLetters.addVerifyListener(new VerifyListener() {
			public void verifyText(final VerifyEvent event) {
				switch (event.keyCode) {
				case SWT.BS:
				case SWT.DEL:
				case SWT.HOME:
				case SWT.END:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_RIGHT:
					return;
				}
				if (!Character.isLowerCase(event.character)) {
					event.doit = false;
				}
			}
		});

		Label labelDictFile = new Label(shell, SWT.NONE);
		labelDictFile.setText("Dictionary File Name:");
		final Text textDictFile = new Text(shell, SWT.BORDER);
		textDictFile.setText("/Users/bdowns/Documents/dictionary.txt");
		Button btnDictFileSelect = new Button(shell, SWT.PUSH);
		btnDictFileSelect.setText("...");
		btnDictFileSelect.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				String[] filterNames = new String[] { "Text Files", "Dict Files",
						"All Files (*)" };
				String[] filterExtensions = new String[] { "*.txt", "*.dic", "*" };
				dialog.setFilterNames(filterNames);
				dialog.setFilterExtensions(filterExtensions);
				dialog.setFileName("dictionary.txt");
				String strFile = dialog.open();
				if (strFile != null) {
					textDictFile.setText(strFile);
				}
			}
		});

		final Button btnStart = new Button(shell, SWT.PUSH);
		final Button btnStop = new Button(shell, SWT.PUSH);
		final List listWordsFound = new List(shell, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL);
		final Text textComboAttempted = new Text(shell, SWT.READ_ONLY | SWT.BORDER);
		final Text textMatchesFound = new Text(shell, SWT.READ_ONLY | SWT.BORDER);
		Text textSearchTime = new Text(shell, SWT.READ_ONLY | SWT.BORDER);

		btnStart.setText("Start");
		btnStart.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int iMin = Integer.parseInt(textMin.getText());
				int iMax = Integer.parseInt(textMax.getText());
				int iCount = textLetters.getText().length();

				if (iMin > iMax) {
					MessageBox messageBox = new MessageBox(shell,
							SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("ERROR");
					messageBox.setMessage("Min is more than Max.");
					messageBox.open();
					return;
				}

				if (iMin > iCount) {
					MessageBox messageBox = new MessageBox(shell,
							SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("ERROR");
					messageBox.setMessage("Min is more than total letters.");
					messageBox.open();
					return;
				}

				if (iMax > iCount) {
					MessageBox messageBox = new MessageBox(shell,
							SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("ERROR");
					messageBox.setMessage("Max is more than total letters.");
					messageBox.open();
					return;
				}

				File fDict = new File(textDictFile.getText());
				if (!fDict.canRead()) {
					MessageBox messageBox = new MessageBox(shell,
							SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("ERROR");
					messageBox.setMessage("Cannot read dictionary file: "
							+ textDictFile.getText());
					messageBox.open();
					return;
				}

				textLetters.setEnabled(false);
				textDictFile.setEnabled(false);
				btnStart.setEnabled(false);
				textMin.setEnabled(false);
				textMax.setEnabled(false);
				btnStop.setEnabled(true);
				listWordsFound.removeAll();

				BufferedReader br = null;
				HashSet<String> hsDict = new HashSet<String>();
				try {
					br = new BufferedReader(new FileReader(fDict));

					String line;
					while ((line = br.readLine()) != null) {
						hsDict.add(line);
					}
					br.close();
				} catch (FileNotFoundException fnfe) {
					fnfe.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} finally {
					br = null;
				}

				HashSet<String> hsCombos = new HashSet<String>();
				String sLetters = textLetters.getText();
				boolean[] bUsed = new boolean[sLetters.length()];
				for (int i = iMin; i <= iMax; ++i) {
					makeCombinations("", i, sLetters, hsCombos, bUsed);
				}

				Iterator<String> iter = hsCombos.iterator();
				while (iter.hasNext()) {
					String sIter = iter.next();
					if (hsDict.contains(sIter))
						listWordsFound.add(sIter);
				}

				textComboAttempted.setText(String.valueOf(hsCombos.size()));
				textMatchesFound.setText(String.valueOf(listWordsFound.getItemCount()));
			}
		});
		btnStop.setText("Stop");
		btnStop.setEnabled(false);
		btnStop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				textLetters.setEnabled(true);
				textDictFile.setEnabled(true);
				btnStart.setEnabled(true);
				textMin.setEnabled(true);
				textMax.setEnabled(true);
				btnStop.setEnabled(false);

				System.out.println("stop");
			}
		});

		Label labelComboAttempted = new Label(shell, SWT.NONE);
		labelComboAttempted.setText("Combo Attempted:");
		textComboAttempted.setText("0 of 0");

		Label labelMatchesFound = new Label(shell, SWT.NONE);
		labelMatchesFound.setText("Matches Found:");
		textMatchesFound.setText("0");

		Label labelSearchTime = new Label(shell, SWT.NONE);
		labelSearchTime.setText("Search Time:");
		final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		textSearchTime.setText(timeFormat.format(date));

		listWordsFound.add("none");
		listWordsFound.setBounds(0, 0, 1200, 520);

		Menu menuListPopup = new Menu(shell, SWT.POP_UP);
		MenuItem itemLookupWord = new MenuItem(menuListPopup, SWT.PUSH);
		itemLookupWord.setText("Lookup Word...");
		itemLookupWord.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int iIndex = listWordsFound.getSelectionIndex();
				if (iIndex != -1) {
					String strWord = listWordsFound.getItem(iIndex);
					Program.launch("http://dictionary.reference.com/browse/"
							+ strWord);
				}
			}
		});
		MenuItem itemEmailResults = new MenuItem(menuListPopup, SWT.PUSH);
		itemEmailResults.setText("Email Results...");
		itemEmailResults.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				final Shell dialogEmail = new Shell(shell, SWT.DIALOG_TRIM
						| SWT.APPLICATION_MODAL);
				dialogEmail.setText("Email Results");
				FormLayout formLayout = new FormLayout();
				formLayout.marginWidth = 10;
				formLayout.marginHeight = 10;
				formLayout.spacing = 10;
				dialogEmail.setLayout(formLayout);

				Label labelEmailAddress = new Label(dialogEmail, SWT.NONE);
				labelEmailAddress.setText("Email Address:");
				FormData data = new FormData();
				labelEmailAddress.setLayoutData(data);

				Button cancelEmail = new Button(dialogEmail, SWT.PUSH);
				cancelEmail.setText("Cancel");
				data = new FormData();
				data.width = 60;
				data.right = new FormAttachment(100, 0);
				data.bottom = new FormAttachment(100, 0);
				cancelEmail.setLayoutData(data);
				cancelEmail.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						System.out.println("User cancelled dialog");
						dialogEmail.close();
					}
				});

				final Text textEmailAddress = new Text(dialogEmail, SWT.BORDER);
				textEmailAddress.setText("bruce_downs@yahoo.com");
				data = new FormData();
				data.width = 200;
				data.left = new FormAttachment(labelEmailAddress, 0,
						SWT.DEFAULT);
				data.right = new FormAttachment(100, 0);
				data.top = new FormAttachment(labelEmailAddress, 0, SWT.CENTER);
				data.bottom = new FormAttachment(cancelEmail, 0, SWT.DEFAULT);
				textEmailAddress.setLayoutData(data);

				Button okEmail = new Button(dialogEmail, SWT.PUSH);
				okEmail.setText("OK");
				data = new FormData();
				data.width = 60;
				data.right = new FormAttachment(cancelEmail, 0, SWT.DEFAULT);
				data.bottom = new FormAttachment(100, 0);
				okEmail.setLayoutData(data);
				okEmail.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						String strEmailAddress = textEmailAddress.getText();
						dialogEmail.close();

						String strResults[] = listWordsFound.getItems();
						String strMessage = "Mrs. Heidi,\n\n";
						for (int i = 0; i < listWordsFound.getItemCount(); i++) {
							strMessage += strResults[i] + "\n";
						}

						@SuppressWarnings("serial")
						Properties props = new Properties() {
							{
								put("mail.smtp.auth", "true");
								put("mail.smtp.host", "smtp.gmail.com");
								put("mail.smtp.port", "587");
								put("mail.smtp.starttls.enable", "true");
								put("mail.debug", "true");
							}
						};

						Session session = Session.getInstance(props,
								new Authenticator() {
									@Override
									protected PasswordAuthentication getPasswordAuthentication() {
										return new PasswordAuthentication(
												"bruceadowns", "password");
									}
								});

						try {
							//
							// Creates email message
							//
							Message message = new MimeMessage(session);
							message.setSentDate(new Date());
							message.setFrom(new InternetAddress(
									"bruceadowns@gmail.com"));
							message.setRecipient(Message.RecipientType.TO,
									new InternetAddress(strEmailAddress));
							message.setSubject("Lydia's Letters Dictionator Results");
							message.setText(strMessage);

							Transport.send(message);
						} catch (MessagingException me) {
							me.printStackTrace();
						}
					}
				});

				dialogEmail.setDefaultButton(okEmail);
				dialogEmail.pack();
				dialogEmail.open();
			}
		});
		listWordsFound.setMenu(menuListPopup);

		/*
		 * form data
		 */
		Rectangle rect = shell.getBounds();
		FormData data = new FormData();
		data.top = new FormAttachment(labelTitle, 8);
		labelLetters.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(labelLetters, 8);
		data.width = rect.width / 2;
		data.top = new FormAttachment(labelLetters, 0, SWT.TOP);
		textLetters.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(labelLetters, 8);
		labelMin.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(labelMin, 8);
		data.top = new FormAttachment(labelMin, 0, SWT.TOP);
		data.width = 100;
		textMin.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(textMin, 8);
		data.top = new FormAttachment(textMin, 0, SWT.TOP);
		labelMax.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(labelMax, 8);
		data.top = new FormAttachment(labelMax, 0, SWT.TOP);
		data.width = 100;
		textMax.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(labelMin, 8);
		labelDictFile.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(labelDictFile, 8);
		data.top = new FormAttachment(labelDictFile, 0, SWT.TOP);
		data.width = rect.width / 2;
		textDictFile.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(textDictFile, 8);
		data.top = new FormAttachment(textDictFile, 0, SWT.TOP);
		data.top = new FormAttachment(textDictFile, 0, SWT.TOP);
		btnDictFileSelect.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(labelDictFile, 8);
		btnStart.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(btnStart, 8);
		data.top = new FormAttachment(btnStart, 0, SWT.TOP);
		btnStop.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(btnStart, 8);
		labelComboAttempted.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(labelComboAttempted, 8);
		data.top = new FormAttachment(labelComboAttempted, 0, SWT.TOP);
		data.width = rect.width / 4;
		textComboAttempted.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(labelComboAttempted, 8);
		labelMatchesFound.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(labelMatchesFound, 8);
		data.top = new FormAttachment(labelMatchesFound, 0, SWT.TOP);
		data.width = rect.width / 4;
		textMatchesFound.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(labelMatchesFound, 8);
		labelSearchTime.setLayoutData(data);
		data = new FormData();
		data.left = new FormAttachment(labelSearchTime, 8);
		data.top = new FormAttachment(labelSearchTime, 0, SWT.TOP);
		data.width = rect.width / 4;
		textSearchTime.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(labelSearchTime, 8);
		data.height = 100;
		data.width = rect.width;
		listWordsFound.setLayoutData(data);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
