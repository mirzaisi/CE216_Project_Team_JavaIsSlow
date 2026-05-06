## Handball Baseline

This file defines the handball baseline that the implementation will follow.

### Competition format

- The handball league uses a double round-robin season structure.
- Teams can win, draw, or lose in the regular season.
- Standings use 2 points for a win, 1 point for a draw, and 0 points for a loss.
- Table order is points, goal difference, goals scored, then team name.

### Squad structure

- Each handball team will start with a 16-player roster.
- A valid starting lineup contains exactly 7 players.
- A valid lineup must contain exactly 1 goalkeeper.
- The remaining 6 players are outfield players.
- Bench size is 7.
- Unlimited substitutions are allowed.

### Handball-specific data model

- Handball players will use a handball position model instead of football positions.
- Handball players will use a handball attribute profile focused on shooting, passing, defense, speed, and reflexes.
- Handball coaches will carry handball-specific specialization and rating data.
- Handball tactics and training plans will stay inside the handball module and will not expand shared core with sport-only fields.

### Match behavior

- Handball matches are expected to be higher-scoring than football matches.
- Match simulation must stay stateless and testable.
- Handball tactics and player attributes should affect the simulation result.
- Replay prevention and result consistency must match the shared Match abstraction.

### Weekly flow

- Handball seasons will use the shared weekly progression structure already present in the application layer.
- Fixtures are grouped by week through the shared Fixture model.
- Played matches update standings through a handball standings policy.
- Availability and injury handling are controlled by a handball injury policy.

### Football assumptions that do not carry over

- Football's 11-player lineup does not apply to handball.
- Football's 3-point win system does not apply to handball.
- Football's lower score ranges do not apply to handball.
- Football's limited substitutions do not apply to handball.
