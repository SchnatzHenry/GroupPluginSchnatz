# GroupPluginSchnatz


Hallo Legend-Teammitglied,
hierbei handelt es sich um die Readme zu meinem Bewerbungsplugin.
Es gibt einige Kleinigkeiten zu beachten.

Es lässt sich alles in der config.yml Datei einstellen, die bei der ersten Ausführung des Plugins erstellt wird.
(Eine kleine Ausnahme gibt es, die JUnitTests wurden für mein setup erstellt, hier bitte einfach die Konstanten in der Klasse ändern.

Die line coverage lag zuletzt bei 19%, getestet wurden alle wichtigen Methoden der Klasse "DatabaseManager".



Die Permission sind mit Ausnahme von "/sign" per default alle auf OP Rechte gesetzt, dies kann natürlich jederzeit in den Annotationen der Main Klasse angepasst werden.

Erstellte Schilder updaten sich zur Laufzeit (beispielsweise wenn ein Spieler eine neue Gruppe erhält) und werden beim Herunterfahren des Servers absichtlich wieder gelöscht.
Da ein Spieler mehrere Gruppen besitzen kann habe ich Gruppenlevel eingeführt. Diese geben an wie stark eine Gruppe gewichtet wird.
Der Präfix des Nutzers bezieht sich entsprechend auf die Gruppe mit dem höchsten Gruppenlevel.
Bei Gleichstand entscheidet momentan noch der Zufall/ die Reihenfolge in der Datenbank welche Gruppe gewertet wird.

Da ich leider keine weitere Zeit habe um sie in das Plugin zu stecken (fahren morgen den 19.09.22 in den Urlaub) wurden solche Kleinigkeiten absichtlich nicht beachtet.
Alle geforderten Funktionen funktionieren und wurden bereits getestet.
Die O-Notation entfällt aus gleichem Grund, ich bitte hierbei um Nachsicht. Bei Bedarf kann ich diese natürlich noch nachreichen.

Viel Spaß mit dem Plugin und vielen Dank, dass Du Dir die Zeit nimmst es anzuschauen.

Mit freundlichen Grüßen,
Henry Schnatz
