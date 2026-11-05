-- PLACEHOLDER CONTENT. Edit freely and restart: repeatable migrations re-run when this file changes.
INSERT INTO lessons (id, title, description, position, published)
VALUES ('dijkstra', 'Dijkstra''s Algorithm', 'Learn how shortest paths are found, step by step.', 1, TRUE)
ON CONFLICT (id) DO UPDATE
    SET title = EXCLUDED.title,
        description = EXCLUDED.description,
        position = EXCLUDED.position,
        published = EXCLUDED.published;

DELETE FROM activities WHERE lesson_id = 'dijkstra';

INSERT INTO activities
    (lesson_id, position, type, title, instruction, content, answer, praise_message, clue_message, explanation)
VALUES
('dijkstra', 0, 'EXPLANATION', 'How Dijkstra works',
 'Step through the algorithm to see how shortest paths are found.',
 '{"graph":{"directed":false,"nodes":[{"id":"A","name":"A","x":100,"y":200},{"id":"B","name":"B","x":250,"y":100},{"id":"C","name":"C","x":250,"y":300},{"id":"D","name":"D","x":400,"y":200}],"edges":[{"id":"AB","source":"A","target":"B","weight":4},{"id":"AC","source":"A","target":"C","weight":2},{"id":"BC","source":"B","target":"C","weight":1},{"id":"BD","source":"B","target":"D","weight":5},{"id":"CD","source":"C","target":"D","weight":8}]},"steps":[{"title":"Step 1","text":"Placeholder: set the source distance to 0 and every other node to infinity."},{"title":"Step 2","text":"Placeholder: pick the unvisited node with the smallest distance."},{"title":"Step 3","text":"Placeholder: update the distances of its neighbors."}]}'::jsonb,
 NULL, '', '', ''),

('dijkstra', 1, 'MULTIPLE_CHOICE', 'First pick',
 'Which node does Dijkstra pick first?',
 '{"options":[{"id":"A","text":"A (the source, distance 0)"},{"id":"B","text":"B"},{"id":"C","text":"C"}]}'::jsonb,
 ARRAY['A'],
 'Correct! The source always starts with distance 0.',
 'Hint: which node has a known distance at the very start?',
 'Every other node starts at infinity, so the source is the only candidate.'),

('dijkstra', 2, 'EDGE_SELECTION', 'Cheapest edge from A',
 'Select the cheapest edge leaving A.',
 '{"graph":{"directed":false,"nodes":[{"id":"A","name":"A","x":100,"y":200},{"id":"B","name":"B","x":250,"y":100},{"id":"C","name":"C","x":250,"y":300},{"id":"D","name":"D","x":400,"y":200}],"edges":[{"id":"AB","source":"A","target":"B","weight":4},{"id":"AC","source":"A","target":"C","weight":2},{"id":"BC","source":"B","target":"C","weight":1},{"id":"BD","source":"B","target":"D","weight":5},{"id":"CD","source":"C","target":"D","weight":8}]},"maxSelections":1}'::jsonb,
 ARRAY['AC'],
 'Nice! A to C costs only 2.',
 'Hint: compare the weights of the edges that touch A.',
 'A-B costs 4 and A-C costs 2, so A-C is cheaper.'),

('dijkstra', 3, 'NODE_SELECTION', 'Next node',
 'After A, which node does Dijkstra visit next?',
 '{"graph":{"directed":false,"nodes":[{"id":"A","name":"A","x":100,"y":200},{"id":"B","name":"B","x":250,"y":100},{"id":"C","name":"C","x":250,"y":300},{"id":"D","name":"D","x":400,"y":200}],"edges":[{"id":"AB","source":"A","target":"B","weight":4},{"id":"AC","source":"A","target":"C","weight":2},{"id":"BC","source":"B","target":"C","weight":1},{"id":"BD","source":"B","target":"D","weight":5},{"id":"CD","source":"C","target":"D","weight":8}]},"maxSelections":1}'::jsonb,
 ARRAY['C'],
 'Right! C has the smallest distance (2).',
 'Hint: after visiting A, B has distance 4. What distance does C have?',
 'Dijkstra always visits the unvisited node with the smallest known distance.'),

('dijkstra', 4, 'CODE_COMPLETION', 'Relax an edge',
 'Complete the code that updates the neighbor''s shortest distance.',
 '{"lines":[{"text":"if (distance[current] + weight < distance[neighbor]) {"},{"blank":true},{"text":"}"}],"tokens":[{"id":"t_dn","text":"distance[neighbor]"},{"id":"t_eq","text":"="},{"id":"t_dc","text":"distance[current]"},{"id":"t_plus","text":"+"},{"id":"t_w","text":"weight"},{"id":"t_semi","text":";"},{"id":"t_return","text":"return"},{"id":"t_while","text":"while"},{"id":"t_visited","text":"visited"},{"id":"t_one","text":"+1"}]}'::jsonb,
 ARRAY['t_dn','t_eq','t_dc','t_plus','t_w','t_semi'],
 'Well done! You just wrote the core of Dijkstra.',
 'Hint: assign the neighbor the cost of reaching the current node plus the edge weight.',
 'distance[neighbor] = distance[current] + weight; stores the shorter path.'),

('dijkstra', 5, 'EXPLORE', 'Explore Dijkstra step by step',
 'Follow the highlighted graph states to see how Dijkstra finds the shortest paths from A.',
 '{"graph":{"directed":false,"nodes":[{"id":"A","name":"A","x":100,"y":200},{"id":"B","name":"B","x":250,"y":100},{"id":"C","name":"C","x":250,"y":300},{"id":"D","name":"D","x":400,"y":200}],"edges":[{"id":"AB","source":"A","target":"B","weight":4},{"id":"AC","source":"A","target":"C","weight":2},{"id":"BC","source":"B","target":"C","weight":1},{"id":"BD","source":"B","target":"D","weight":5},{"id":"CD","source":"C","target":"D","weight":8}]},"exploreSteps":[{"title":"Start at A","explanation":"We begin at A with distance 0. Every other node is still unknown (∞), so A is the only node we can choose first.","currentNode":"A","visitedNodes":[],"distances":{"A":"0","B":"∞","C":"∞","D":"∞"},"consideredEdges":[],"selectedEdges":[]},{"title":"Check A’s neighbors","explanation":"A has the smallest tentative distance (0), so we finalize it. The edge A–B gives distance 4, and A–C gives distance 2. These are tentative routes, not final choices yet.","currentNode":"A","visitedNodes":["A"],"distances":{"A":"0","B":"4","C":"2","D":"∞"},"consideredEdges":["AB","AC"],"selectedEdges":["AB","AC"]},{"title":"Choose C and improve B","explanation":"C is next because its tentative distance 2 is smaller than B’s 4. Through C, B can be reached in 2 + 1 = 3, improving its distance from 4 to 3. C also gives D a tentative distance of 2 + 8 = 10.","currentNode":"C","visitedNodes":["A","C"],"distances":{"A":"0","B":"3","C":"2","D":"10"},"consideredEdges":["BC","CD"],"selectedEdges":["AC","BC","CD"]},{"title":"Choose B and improve D","explanation":"B is now the closest unvisited node at distance 3, so we finalize it. The B–C edge is ignored because C is already finalized. Going through B reaches D in 3 + 5 = 8, improving D from 10 to 8.","currentNode":"B","visitedNodes":["A","C","B"],"distances":{"A":"0","B":"3","C":"2","D":"8"},"consideredEdges":["BC","BD"],"selectedEdges":["AC","BC","BD"]},{"title":"Finalize D","explanation":"D is the only unvisited node and its best-known distance is 8, so that distance is final. All nodes are now finalized. The shortest route from A to D follows A–C–B–D.","currentNode":"D","visitedNodes":["A","C","B","D"],"distances":{"A":"0","B":"3","C":"2","D":"8"},"consideredEdges":[],"selectedEdges":["AC","BC","BD"]}]} '::jsonb,
 NULL,
 'You explored every step of Dijkstra’s algorithm.',
 '',
 'The final shortest distance from A to D is 8, along A–C–B–D.');
