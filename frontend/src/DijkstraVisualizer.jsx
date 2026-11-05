import React, { useState, useEffect } from 'react';

// Color mapping matching your NodeState enum & Figma legend
const NODE_STATE_STYLES = {
  SOURCE: { bg: 'bg-emerald-500', border: 'border-emerald-700', text: 'text-white', label: 'Source' },
  CURRENT: { bg: 'bg-amber-400', border: 'border-amber-600', text: 'text-slate-900', label: 'Current' },
  NEIGHBOR: { bg: 'bg-sky-400', border: 'border-sky-600', text: 'text-slate-900', label: 'Neighbor' },
  VISITED: { bg: 'bg-indigo-500', border: 'border-indigo-700', text: 'text-white', label: 'Visited' },
  UNVISITED: { bg: 'bg-slate-200', border: 'border-slate-400', text: 'text-slate-700', label: 'Unvisited' }
};

// Hardcoded (x,y) positions for rendering the demo graph on SVG canvas
const NODE_POSITIONS = {
  A: { x: 100, y: 150 },
  B: { x: 300, y: 70 },
  C: { x: 300, y: 230 },
  D: { x: 500, y: 150 }
};

export default function DijkstraVisualizer() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [sourceNode, setSourceNode] = useState('A');

  // Fetch graph & step snapshots from Spring Boot backend
  useEffect(() => {
    setLoading(true);
    fetch(`http://localhost:8080/api/demo/dijkstra?source=${sourceNode}`)
      .then(res => {
        if (!res.ok) throw new Error('Failed to connect to backend engine');
        return res.json();
      })
      .then(json => {
        setData(json);
        setCurrentStepIndex(0);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, [sourceNode]);

  // Auto-play timer loop
  useEffect(() => {
    let timer;
    if (isPlaying && data && currentStepIndex < data.result.steps.length - 1) {
      timer = setTimeout(() => {
        setCurrentStepIndex(prev => prev + 1);
      }, 1200);
    } else if (currentStepIndex === data?.result?.steps?.length - 1) {
      setIsPlaying(false);
    }
    return () => clearTimeout(timer);
  }, [isPlaying, currentStepIndex, data]);

  if (loading) return <div className="p-8 text-center text-slate-600 font-semibold">Loading Dijkstra Engine...</div>;
  if (error) return <div className="p-8 text-center text-red-500 font-semibold">Error: {error}</div>;

  const steps = data.result.steps;
  const currentStep = steps[currentStepIndex];
  const graph = data.graph;

  return (
    <div className="max-w-5xl mx-auto p-6 font-sans text-slate-800">
      <header className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">AlgoVista — Dijkstra Visualizer</h1>
          <p className="text-sm text-slate-500">Interactive frontend consuming backend step snapshots</p>
        </div>
        <div className="flex items-center gap-2">
          <label className="text-sm font-medium">Source Node:</label>
          <select 
            value={sourceNode} 
            onChange={(e) => setSourceNode(e.target.value)}
            className="border border-slate-300 rounded px-2 py-1 text-sm bg-white shadow-sm"
          >
            {graph.nodes.map(n => <option key={n.id} value={n.id}>{n.id}</option>)}
          </select>
        </div>
      </header>

      {/* Step Info Banner */}
      <div className="bg-slate-900 text-white rounded-lg p-4 mb-6 shadow-md">
        <div className="flex justify-between items-center mb-2">
          <span className="text-xs font-bold tracking-wider text-amber-400 uppercase">
            Step {currentStep.index + 1} of {steps.length} — {currentStep.type}
          </span>
          <span className="text-xs text-slate-400">Node: {currentStep.currentNodeId || 'N/A'}</span>
        </div>
        <p className="text-base text-slate-100 font-medium">{currentStep.description}</p>
      </div>

      {/* Main Grid: Graph Canvas & Distance Table */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        {/* SVG Graph Canvas */}
        <div className="md:col-span-2 bg-white border border-slate-200 rounded-xl p-4 shadow-sm relative">
          <h2 className="text-xs font-semibold text-slate-400 uppercase mb-2">Graph Visualization</h2>
          <svg className="w-full h-72 border border-slate-100 rounded-lg bg-slate-50" viewBox="0 0 600 300">
            {/* Render Edges */}
            {graph.edges.map(edge => {
              const u = NODE_POSITIONS[edge.source];
              const v = NODE_POSITIONS[edge.target];
              const isRelaxing = currentStep.edgeId === edge.id;
              
              return (
                <g key={edge.id}>
                  <line 
                    x1={u.x} y1={u.y} 
                    x2={v.x} y2={v.y} 
                    stroke={isRelaxing ? '#f59e0b' : '#cbd5e1'} 
                    strokeWidth={isRelaxing ? '4' : '2'} 
                  />
                  {/* Edge Weight Badge */}
                  <rect 
                    x={(u.x + v.x) / 2 - 12} 
                    y={(u.y + v.y) / 2 - 10} 
                    width="24" height="20" 
                    rx="4" fill="#ffffff" stroke="#e2e8f0" 
                  />
                  <text 
                    x={(u.x + v.x) / 2} 
                    y={(u.y + v.y) / 2 + 4} 
                    textAnchor="middle" 
                    className="text-xs font-bold fill-slate-600"
                  >
                    {edge.weight}
                  </text>
                </g>
              );
            })}

            {/* Render Nodes */}
            {graph.nodes.map(node => {
              const pos = NODE_POSITIONS[node.id];
              const stateKey = currentStep.nodeStates[node.id] || 'UNVISITED';
              const style = NODE_STATE_STYLES[stateKey];
              const dist = currentStep.distances[node.id];

              return (
                <g key={node.id} transform={`translate(${pos.x}, ${pos.y})`}>
                  <circle 
                    r="22" 
                    className={`${style.bg} ${style.border} stroke-2 transition-all duration-300`} 
                  />
                  <text textAnchor="middle" dy="-2" className={`text-sm font-bold ${style.text}`}>
                    {node.id}
                  </text>
                  <text textAnchor="middle" dy="12" className={`text-[10px] font-semibold ${style.text}`}>
                    {dist === null ? '∞' : `d=${dist}`}
                  </text>
                </g>
              );
            })}
          </svg>

          {/* Legend */}
          <div className="flex justify-around items-center mt-3 text-xs">
            {Object.entries(NODE_STATE_STYLES).map(([key, style]) => (
              <div key={key} className="flex items-center gap-1">
                <span className={`w-3 h-3 rounded-full ${style.bg}`} />
                <span className="text-slate-600">{style.label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Live Distance Table */}
        <div className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm">
          <h2 className="text-xs font-semibold text-slate-400 uppercase mb-3">Shortest Distance Table</h2>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-left text-slate-500">
                <th className="pb-2">Node</th>
                <th className="pb-2">Distance</th>
                <th className="pb-2">Previous</th>
              </tr>
            </thead>
            <tbody>
              {graph.nodes.map(n => {
                const dist = currentStep.distances[n.id];
                const prev = currentStep.previous[n.id];
                const isCurrent = currentStep.currentNodeId === n.id;
                
                return (
                  <tr key={n.id} className={`border-b border-slate-100 ${isCurrent ? 'bg-amber-50 font-semibold' : ''}`}>
                    <td className="py-2 text-slate-800">{n.id}</td>
                    <td className="py-2 text-slate-600">{dist === null ? '∞' : dist}</td>
                    <td className="py-2 text-slate-400">{prev || '-'}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Stepper Controls */}
      <div className="flex items-center justify-between bg-slate-50 border border-slate-200 rounded-xl p-4 shadow-sm">
        <button 
          onClick={() => setIsPlaying(!isPlaying)}
          className="px-4 py-2 bg-indigo-600 text-white font-medium text-sm rounded-lg hover:bg-indigo-700 transition"
        >
          {isPlaying ? 'Pause' : 'Play Simulation'}
        </button>

        <div className="flex items-center gap-2">
          <button 
            disabled={currentStepIndex === 0}
            onClick={() => { setIsPlaying(false); setCurrentStepIndex(0); }}
            className="px-3 py-1.5 bg-white border border-slate-300 rounded text-sm disabled:opacity-40"
          >
            Reset
          </button>
          <button 
            disabled={currentStepIndex === 0}
            onClick={() => { setIsPlaying(false); setCurrentStepIndex(prev => prev - 1); }}
            className="px-3 py-1.5 bg-white border border-slate-300 rounded text-sm disabled:opacity-40"
          >
            Prev Step
          </button>
          <button 
            disabled={currentStepIndex === steps.length - 1}
            onClick={() => { setIsPlaying(false); setCurrentStepIndex(prev => prev + 1); }}
            className="px-3 py-1.5 bg-white border border-slate-300 rounded text-sm disabled:opacity-40"
          >
            Next Step
          </button>
        </div>

        {/* Step Scrubber Slider */}
        <input 
          type="range" 
          min="0" 
          max={steps.length - 1} 
          value={currentStepIndex} 
          onChange={(e) => { setIsPlaying(false); setCurrentStepIndex(Number(e.target.value)); }}
          className="w-48 accent-indigo-600"
        />
      </div>
    </div>
  );
}
