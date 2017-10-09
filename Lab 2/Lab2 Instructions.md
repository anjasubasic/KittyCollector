# Lab 2: Communicate with a Network Server; Tabbed UI

## Concepts:

- Retrieve data via simple HTTP connections
- Create a tab-based UI frame for the following labs
- Utilize a PreferenceFragment standard library view

## Tasks:

1. In the sign-up activity of Lab 1, implement the check for the
   availability of a username by making an HTTP connection to the
   server. See request format specification in server-protocol.txt.

2. In the sign-up activity of Lab 1, implement uploading of
   the user profile data. See upload format specification in
   server-protocol.txt. Process the server response and display
   a success message or error message (see server-protocol.txt
   for the format of these messages).

   *NOTE: Make sure to use AsyncTask or similar methods that
   offload network communication tasks to separate threads.
   We may inject deliberate slowdown into your server
   communications to test that your UI thread remains
   responsive!*

3. Implement a sign-in screen (see specification in
   server-protocol.txt) and a preferences screen as outlined in
   Lab2.png mockup.

4. Implement the skeleton tabbed layout of the application.
   Leave tabs other than Settings empty; they will be filled
   in the following labs.

5. For extra credit implement the ability to update the account details on
the server from the preferences tab. Once your account is created, you will
need a matching character name and password to the one stored on the server to
do so. See specification in server-protocol.txt to do so.

## Terms and conditions:

This assignment will be due next Tuesday (October 10th) at 11:59 PM EST.
Late assignments will get 10% credit docked for each day they are late.
However, you will receive three (3) free 24-hour late submission passes for this course. Use
them wisely. As usual, we will waive late penalties for extenuating
circumstances such as travel and sickness (ask if in doubt).

The user story below is intended as a general guidance to creating a sensible app interaction.
Feel free to use your judgement as to what makes sense and is intuitive.

Although you are free to explore third party libraries, the TAs will be able to
provide limited help with issues with them.

Extra credit will count for 20% of extra points.

## User Stories:

This user story is enhanced from Lab 1. New elements are marked
with "L2:"

1. Opening the app on first launch results in launching the
   account sign-in UI (Step 1 in Lab2.png).
   If you previously created an account you should be
   able to enter the character name and password and be taken to the Main application.
   If you click on "Create a new account" as shown in
   Lab2.png, the app should take you to the sign-up activity that you implemented in Lab 1.

   The settings fragment of the Main app will contain a "Sign Out" button. Clicking it
   should clear the information out of local storage and take you to the sign-in screen.
   You will be expected to download the profile information from the server to
   repopulate the local settings.

2. On clicking "Create a new account", a user is presented with a form as shown in the Lab 1 mockup for account
   info entry.

   L2: This time, implement the tabbed layout (or equivalent ActionBar
   functionality). Leave the tabs except the Settings tab empty.

3. A user must type in a character name, full name, and passwords.

   L2: After the user enters a character name, the app automatically
   makes a request to the server to check if the name is
   available. Right after the user enters the character name
   (eg. the EditText loses focus, the app automatically makes a request to the server).
   If the server response indicates availability, show
   confirmation to the user (e.g., a green check mark icon). If
   the server indicates the name is not available, prompt the user
   to try a new one.

4. A user may optionally set a profile picture taken by the phone
   camera.

5. After a user enters their password, a popup appears to ask
   them to reconfirm it.

     - The password reconfirm prompt displays a "matches" indication
       when the two passwords entered match.

     - A user may close the box if they don't match to renter the
       first one again.

     - A user may not click save until they match.

6. When a user click the save button, all the inputs should be validated.
   In particular, all the fields should be filled, the username should be
   available and the password should be confirmed.
   A user failing to enter information as required will be presented
   with an informative error message about what was entered wrong.
   The "Save" action should NOT upload the profile to the server in this case.
   By standard convention, you may apply a red border to the erroneous fields
   and display text below them. You may also find other ways to achieve the
   same effect. Note that the server will also validate the profile and send
   you an error message. You should handle that.

7. L2: Clicking the "Save" button should now upload the profile
     to the server, and present the user either with the confirmation
     that the profile has been successfully registered, or an
     error message.
	All the inputs should be submitted to the server.
	If the server reports success, the inputs should also be saved locally.
	Preserving inputs not successfully saved by the server is at your discretion.
   For example, someone may have claimed the username after the
   user last checked it was free. Alternatively, the server may
   have encountered an internal error and responded that it was
   not able to save the profile. These things happen.

	Show the user a message that profile data is being uploaded.
	Optionally (for extra credit) you can implement a progress indicator showing
	the progress of the upload. This will come in handy when
	uploading images. Alert the user the final status of the upload (sucess or failure).

8. The user has the option of clicking "I already have an account".

   L2: This should bring the user to the log-in page. See Lab2.png.
   The "Sign In" button may be disabled for this lab.
   You will implement the actual log-in procedure in the next lab,
   using HTTPS.

9. Once the user starts entering data, the "I already have an account"
   changes to a "Clear" button that allows them to reset all entered
   data to the default. The "default" may be interpreted as "load previously
   entered data from SharedPreferences" or "Clear all fields".

10. L2: Clicking on the tab headings (or action bar items) should bring
    up the respective tabs. Swiping left and right should also bring
    up the respective tabs. Leave all tabs but Settings filled
    with dummy content; you will fill them in the following labs,
    e.g., with a Google Maps-based view for Play.

11. L2: The settings tab displays user selected settings such as
    whether sound or vibrate cues are used in the game for proximity
    alerts at various distances from the target/treasure, and
    whether the user's achievements are displayed on a server's
    public scoreboard. Look at Lab2.png and you may get some idea
about how you might layout these settings.
