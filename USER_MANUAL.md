# PlayForge Manager — User Manual

This document walks you through using PlayForge Manager from the moment you launch it to the end of a season. The same flow works for both Football and Handball, so the screens and buttons are the same regardless of which sport you pick.

## Starting the app

When you launch PlayForge Manager you land on the home screen. There are two buttons here. The first one starts a brand new game, and the second one opens an existing save file. If this is your first time using the app, pick the first one.

## Choosing a sport

The new game screen asks you to pick a sport and to enter a name for your league. There are two toggle buttons, one for Football and one for Handball. Pick whichever you want, type a league name in the text field if you do not like the default one, and press the start button. The app generates a fresh league of four teams for you, fills each team with players and a coach, and lands you on the team overview screen of the team you control.

If you change your mind there is a back button that takes you to the home screen.

## The sidebar

Once you are inside a season the left side of the window shows a sidebar with the navigation. The Home and New Game entries are always available. The other entries become active once a session is loaded. They are Team, Squad, Tactics and Lineup, Fixtures, League Table, Next Match, and Save and Load. You can move between these screens at any time and your progress is kept in memory until you save.

The top of the window shows the sport, the team you control, and the current week of the season.

## Team Overview

The team overview is the home base for your season. It shows your team name, where you sit in the league, how many players you have on the roster, how many of those are available, the current lineup size, the tactic you are using, and the training focus. It also shows the next match coming up.

There are three buttons at the bottom that take you to the squad list, the tactics and lineup screen, and the next match.

## Squad

The squad screen is a table of every player on your team. Each row shows the player name, their role (sport-specific position), whether they are available or out, and whether they are part of the currently selected lineup. Injured players show how many matches they are out for.

This screen is read-only for now. You manage the lineup from the tactics screen.

## Tactics and Lineup

The tactics and lineup screen is where you set your team up before a match. It is split into three blocks. The first block is the starting lineup, the second block is the bench, and the third block is the tactic.

At the top of the screen there is an Auto-pick lineup button. If you press it, the app picks a valid lineup for you using only the players that are available. If a player became injured or unavailable, this is the easiest way to clean up a stale lineup.

Below that, the screen tells you whether the current lineup is valid for the sport you are playing. If something is wrong, like a missing position or an unavailable player in the lineup, the message explains what.

The tactic block on the right shows the name of the tactic that is currently selected, a short description, and a list of presets. Pressing any preset switches your team to that tactic. The currently selected one is highlighted.

## Fixtures

The fixtures screen shows the full season schedule. Each row is one match: the week, the home team, the away team, the score if the match has been played, the status, and a tag if your own team is involved. Played matches show their result, upcoming matches show a placeholder.

## League Table

The league table shows the standings. The columns are the rank, the team name, the matches played, wins, draws, losses, scores for, scores against, the difference, and the points. The points are calculated using the rules of the sport you picked, so Football uses three points for a win and Handball uses two.

## Next Match

The next match screen is where you actually play the next fixture for your team. The top of the screen tells you who you are playing and whether you are at home or away. Below that you see your current tactic and your current lineup status.

There are two action buttons. Play match runs the match for your team using the lineup and tactic you set, and takes you to the post-match screen. If your lineup is not valid the button is disabled and you need to fix it on the tactics screen first.

The other button is Advance week. If you press it, the app simulates the whole week, including your match, using the current setup. Use this when you do not want to micromanage every match.

## Match Result

After a match finishes you see the post-match screen. It tells you whether you won, drew, or lost, shows the score with both teams, and tells you where you sit in the league after the match. Below that there is a list of squad availability changes. If a player on either team got injured or recovered, that change shows up here with a coloured note.

From this screen you can advance to the next week, jump to the league table, or go to the fixtures screen.

## Week Summary

If you advance the week from the next match screen, you land on a week summary screen. This screen lists all the matches that were played this week and any availability changes that happened across the league. From here you continue to the next week.

## Saving and loading

Use the Save and Load entry in the sidebar to save your current game or to load a previous one.

When you save, you are asked where on disk to put the file. The app picks the right extension for you, which is `.pfm-save.json`. You can move this file around freely, copy it to another machine, or back it up.

When you load, you point the app at a save file and it reconstructs your session. After a successful load the app jumps you to the team overview of the controlled team, exactly as you left it.

If a save file is broken, missing fields, or from an unsupported version, the app shows you a clear error message and does not change your current session.

## End of the season

Once every fixture has been played the season is marked as completed. The next match button on the team overview shows that the season is finished, and the league table shows the final standings. From here you can save the game in its completed state, or start a new game from the home screen.
