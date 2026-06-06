import React, { useState, useEffect, useRef } from 'react';
import { 
  Upload, Sparkles, BookOpen, Terminal, CheckCircle2, ChevronRight, MessageSquare, 
  Send, HelpCircle, FileText, BarChart3, AlertCircle, RefreshCw, Layers, Award,
  ArrowRight, ShieldCheck, Zap, User, Clock, CheckSquare, PlayCircle, Eye, EyeOff
} from 'lucide-react';

const API_BASE = window.location.origin.includes('5173') 
  ? 'http://localhost:8080/api' 
  : window.location.origin + '/api';

export default function App() {
  // Navigation & Page State
  const [view, setView] = useState('landing'); // 'landing' | 'loading' | 'dashboard'
  const [activeTab, setActiveTab] = useState('overview'); // overview, match, ats, skills, roadmap, interview, twin
  const [loadingMessage, setLoadingMessage] = useState('');

  // Form inputs
  const [file, setFile] = useState(null);
  const [jobTitle, setJobTitle] = useState('');
  const [jobDescription, setJobDescription] = useState('');

  // Data states (from API)
  const [resume, setResume] = useState(null);
  const [analysis, setAnalysis] = useState(null);
  const [roadmap, setRoadmap] = useState(null);
  const [interviewPack, setInterviewPack] = useState(null);
  const [chatHistory, setChatHistory] = useState([]);
  const [chatInput, setChatInput] = useState('');

  // UI state variables
  const [isUploading, setIsUploading] = useState(false);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [chatLoading, setChatLoading] = useState(false);
  const [visibleAnswers, setVisibleAnswers] = useState({}); // { 'tech-0': true }
  
  const chatEndRef = useRef(null);

  // Auto-scroll chat to bottom
  useEffect(() => {
    if (chatEndRef.current) {
      chatEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [chatHistory, chatLoading]);

  // Load demo data instant trigger
  const handleUseDemoData = async () => {
    setView('loading');
    setLoadingMessage('Initializing CareerPilot demo datasets...');
    try {
      const res = await fetch(`${API_BASE}/demo`, { method: 'POST' });
      if (!res.ok) throw new Error('Demo initialization failed');
      const data = await res.json();
      
      setAnalysis(data);
      setResume(data.resume);
      
      // Fetch associated roadmap and interview packs in parallel
      setLoadingMessage('Compiling learning roadmaps & interview packets...');
      const [roadmapRes, interviewRes, chatRes] = await Promise.all([
        fetch(`${API_BASE}/analysis/${data.id}/roadmap`, { method: 'POST' }),
        fetch(`${API_BASE}/analysis/${data.id}/interview-pack`, { method: 'POST' }),
        fetch(`${API_BASE}/chat/${data.id}`)
      ]);

      if (roadmapRes.ok) setRoadmap(await roadmapRes.json());
      if (interviewRes.ok) setInterviewPack(await interviewRes.json());
      if (chatRes.ok) setChatHistory(await chatRes.json());

      setView('dashboard');
      setActiveTab('overview');
    } catch (error) {
      console.error(error);
      alert('Error initializing demo data. Ensure backend is running.');
      setView('landing');
    }
  };

  // Upload PDF Resume
  const handleFileUpload = async (e) => {
    const uploadedFile = e.target.files[0];
    if (!uploadedFile) return;
    if (uploadedFile.type !== 'application/pdf') {
      alert('Please upload a PDF file.');
      return;
    }
    setFile(uploadedFile);
  };

  const startAnalysis = async (e) => {
    e.preventDefault();
    if (!file) {
      alert('Please select a PDF Resume first.');
      return;
    }
    if (!jobTitle.trim() || !jobDescription.trim()) {
      alert('Please fill out the Job Title and Job Description.');
      return;
    }

    setView('loading');
    setIsUploading(true);
    setLoadingMessage('Parsing resume content using OCR & AI extraction...');

    try {
      // 1. Upload Resume
      const formData = new FormData();
      formData.append('file', file);

      const resumeRes = await fetch(`${API_BASE}/resumes/upload`, {
        method: 'POST',
        body: formData,
      });

      if (!resumeRes.ok) throw new Error('Resume parser failed');
      const parsedResume = await resumeRes.json();
      setResume(parsedResume);

      // 2. Perform Analysis
      setIsUploading(false);
      setIsAnalyzing(true);
      setLoadingMessage('Matching competencies & scoring ATS metrics...');

      const analysisRes = await fetch(`${API_BASE}/analysis`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          resumeId: parsedResume.id,
          jobTitle,
          jobDescription,
        }),
      });

      if (!analysisRes.ok) throw new Error('Job analysis failed');
      const parsedAnalysis = await analysisRes.json();
      setAnalysis(parsedAnalysis);

      // 3. Generate Roadmap & Interview pack in parallel
      setLoadingMessage('Generating tailored 30-day curriculum & Q&As...');
      const [roadmapRes, interviewRes] = await Promise.all([
        fetch(`${API_BASE}/analysis/${parsedAnalysis.id}/roadmap`, { method: 'POST' }),
        fetch(`${API_BASE}/analysis/${parsedAnalysis.id}/interview-pack`, { method: 'POST' }),
      ]);

      if (roadmapRes.ok) setRoadmap(await roadmapRes.json());
      if (interviewRes.ok) setInterviewPack(await interviewRes.json());

      // Fetch Chat
      const chatRes = await fetch(`${API_BASE}/chat/${parsedAnalysis.id}`);
      if (chatRes.ok) setChatHistory(await chatRes.json());

      setView('dashboard');
      setActiveTab('overview');
    } catch (err) {
      console.error(err);
      alert('An error occurred during parsing. Check console or backend logs.');
      setView('landing');
    } finally {
      setIsUploading(false);
      setIsAnalyzing(false);
    }
  };

  // AI Chat Assistant message sending
  const handleSendChatMessage = async (e) => {
    e.preventDefault();
    if (!chatInput.trim() || !analysis) return;

    const messageText = chatInput;
    setChatInput('');
    
    // Add message locally first for instant feedback
    const userMessage = { sender: 'USER', message: messageText, createdAt: new Date().toISOString() };
    setChatHistory(prev => [...prev, userMessage]);
    setChatLoading(true);

    try {
      const res = await fetch(`${API_BASE}/chat/${analysis.id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: messageText }),
      });
      if (!res.ok) throw new Error('Chat failed');
      const data = await res.json();
      setChatHistory(prev => [...prev, data]);
    } catch (error) {
      console.error(error);
      const errorMessage = { sender: 'ASSISTANT', message: 'I was unable to reach the AI engine. Please verify that your Gemini API Key is configured and valid.', createdAt: new Date().toISOString() };
      setChatHistory(prev => [...prev, errorMessage]);
    } finally {
      setChatLoading(false);
    }
  };

  // Helper parser for JSON fields stored in DB
  const parseJsonArray = (jsonString) => {
    try {
      return jsonString ? JSON.parse(jsonString) : [];
    } catch (e) {
      return [];
    }
  };

  // Helpers to toggle interview questions
  const toggleAnswer = (key) => {
    setVisibleAnswers(prev => ({ ...prev, [key]: !prev[key] }));
  };

  // Custom visual components inside App
  const CircularGauge = ({ score, title, colorClass = 'text-brand-500', strokeColor = '#6373e9' }) => {
    const radius = 45;
    const circumference = 2 * Math.PI * radius;
    const strokeDashoffset = circumference - (score / 100) * circumference;

    return (
      <div className="glass-card p-6 flex flex-col items-center justify-center rounded-2xl relative overflow-hidden transition-all duration-300 hover:scale-[1.02]">
        <div className="absolute -right-6 -bottom-6 w-24 h-24 bg-brand-500/5 rounded-full filter blur-xl animate-pulse-slow"></div>
        <div className="relative w-28 h-28">
          <svg className="w-full h-full transform -rotate-90">
            <circle cx="56" cy="56" r={radius} className="text-slate-800" strokeWidth="6" stroke="currentColor" fill="transparent" />
            <circle cx="56" cy="56" r={radius} stroke={strokeColor} strokeWidth="8" fill="transparent"
              strokeDasharray={circumference} strokeDashoffset={strokeDashoffset} strokeLinecap="round" className="transition-all duration-1000 ease-out" />
          </svg>
          <div className="absolute inset-0 flex items-center justify-center">
            <span className="text-2xl font-bold font-display text-white">{score}%</span>
          </div>
        </div>
        <h4 className="mt-4 text-xs font-semibold uppercase tracking-wider text-slate-400 text-center">{title}</h4>
      </div>
    );
  };

  return (
    <>
      {/* NAVBAR */}
      <nav className="glass sticky top-0 z-40 border-b border-slate-800/80 px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2 cursor-pointer" onClick={() => setView('landing')}>
            <div className="bg-gradient-to-tr from-brand-600 to-pink-500 p-2 rounded-xl text-white shadow-lg shadow-brand-500/20">
              <Sparkles className="w-6 h-6 animate-pulse" />
            </div>
            <div>
              <span className="font-extrabold text-xl tracking-tight text-white font-display">CareerPilot <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-400 to-pink-400">AI</span></span>
              <p className="text-[10px] text-slate-400 -mt-1 font-semibold uppercase tracking-widest">Employability Hub</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <button onClick={handleUseDemoData} className="px-4 py-2 text-xs font-bold text-slate-300 hover:text-white glass rounded-xl transition-all flex items-center gap-2 hover:border-slate-600">
              <PlayCircle className="w-4 h-4 text-emerald-400" />
              Use Demo Data
            </button>
            {view === 'dashboard' && (
              <button onClick={() => setView('landing')} className="px-4 py-2 text-xs font-bold bg-slate-800 hover:bg-slate-700 text-white rounded-xl transition-all">
                Reset App
              </button>
            )}
          </div>
        </div>
      </nav>

      {/* VIEW: LANDING PAGE */}
      {view === 'landing' && (
        <div className="flex-1 flex flex-col">
          {/* HERO SECTION */}
          <header className="relative max-w-7xl mx-auto px-6 pt-16 pb-12 text-center flex-1 flex flex-col justify-center items-center">
            <div className="absolute w-96 h-96 bg-brand-500/10 rounded-full filter blur-3xl -top-12 -left-12 -z-10 animate-pulse-slow"></div>
            <div className="absolute w-96 h-96 bg-pink-500/5 rounded-full filter blur-3xl bottom-12 -right-12 -z-10 animate-pulse-slow"></div>

            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full glass border border-brand-500/30 text-brand-300 text-xs font-semibold mb-6 shadow-lg shadow-brand-500/5">
              <Sparkles className="w-4 h-4 text-brand-400 animate-spin-slow" />
              Empowering final-year students & fresh graduates
            </div>

            <h1 className="text-4xl md:text-6xl font-extrabold tracking-tight max-w-4xl leading-tight font-display mb-6">
              Land Better Interviews.<br />
              Build The Right Skills.<br />
              <span className="text-gradient-brand">Get Job Ready.</span>
            </h1>

            <p className="text-slate-300 text-lg md:text-xl max-w-2xl font-light mb-10 leading-relaxed">
              CareerPilot AI connects the dots between your academic background, target job specifications, and hiring readiness metrics using LLM evaluation models.
            </p>

            {/* FLOW / CORE WORKFLOW FORM */}
            <div className="w-full max-w-4xl glass-card rounded-3xl p-6 md:p-8 text-left mb-16 shadow-2xl relative border-brand-500/20">
              <div className="absolute top-0 right-0 w-32 h-32 bg-brand-500/5 rounded-bl-full pointer-events-none"></div>
              <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
                <Terminal className="w-5 h-5 text-brand-400" /> Start Employability Audit
              </h3>
              
              <form onSubmit={startAnalysis} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Left Column: Upload PDF Resume */}
                  <div className="flex flex-col">
                    <label className="text-sm font-semibold text-slate-300 mb-2">1. Upload PDF Resume</label>
                    <div className="flex-1 flex flex-col items-center justify-center border-2 border-dashed border-slate-700/80 hover:border-brand-500/80 rounded-2xl p-6 bg-slate-900/40 cursor-pointer transition-all relative group">
                      <input type="file" accept=".pdf" onChange={handleFileUpload} className="absolute inset-0 opacity-0 cursor-pointer" />
                      <div className="text-center flex flex-col items-center">
                        <div className="p-3 bg-brand-500/10 rounded-xl mb-3 text-brand-400 group-hover:scale-110 transition-transform">
                          <Upload className="w-6 h-6" />
                        </div>
                        {file ? (
                          <div>
                            <p className="text-xs font-bold text-emerald-400 truncate max-w-[200px]">{file.name}</p>
                            <p className="text-[10px] text-slate-400 mt-1 uppercase">Click or drag to change</p>
                          </div>
                        ) : (
                          <div>
                            <p className="text-xs font-semibold text-slate-300">Drag & drop your PDF resume here</p>
                            <p className="text-[10px] text-slate-500 mt-1 uppercase">or click to browse files</p>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Right Column: Job Title & Description */}
                  <div className="flex flex-col space-y-4">
                    <div>
                      <label className="text-sm font-semibold text-slate-300 mb-2 block">2. Target Job Title</label>
                      <input type="text" placeholder="e.g. Junior Fullstack Developer" value={jobTitle} onChange={(e) => setJobTitle(e.target.value)}
                        className="w-full bg-slate-900/80 border border-slate-800 rounded-xl px-4 py-3 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 transition-all" />
                    </div>
                    <div>
                      <label className="text-sm font-semibold text-slate-300 mb-2 block">3. Job Description requirements</label>
                      <textarea rows="4" placeholder="Paste the skills, requirements, and responsibilities mentioned in the Job Description..." value={jobDescription} onChange={(e) => setJobDescription(e.target.value)}
                        className="w-full bg-slate-900/80 border border-slate-800 rounded-xl px-4 py-3 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 transition-all resize-none"></textarea>
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-4">
                  <span className="text-xs text-slate-400 flex items-center gap-1.5">
                    <ShieldCheck className="w-4 h-4 text-emerald-400" />
                    Audits run locally. Data parsing secure.
                  </span>
                  <div className="flex gap-3 w-full sm:w-auto">
                    <button type="button" onClick={handleUseDemoData} className="flex-1 sm:flex-initial px-5 py-3 rounded-xl border border-slate-700 text-xs font-bold text-slate-300 hover:text-white bg-slate-900 hover:bg-slate-850 transition-all flex items-center justify-center gap-2">
                      <PlayCircle className="w-4 h-4 text-emerald-400" /> Use Sample Data
                    </button>
                    <button type="submit" className="flex-1 sm:flex-initial px-6 py-3 rounded-xl bg-gradient-to-r from-brand-600 to-pink-500 hover:from-brand-500 hover:to-pink-400 text-xs font-bold text-white shadow-lg shadow-brand-500/20 hover:scale-[1.02] transition-all flex items-center justify-center gap-2">
                      <Sparkles className="w-4 h-4 animate-pulse" /> Audit Employability
                    </button>
                  </div>
                </div>
              </form>
            </div>

            {/* SECTIONS: FEATURESGRID */}
            <div className="w-full max-w-6xl grid grid-cols-1 md:grid-cols-3 gap-8 text-left mb-20">
              <div className="glass p-8 rounded-3xl relative overflow-hidden group border-slate-800/60">
                <div className="p-3 bg-brand-500/10 rounded-2xl w-fit text-brand-400 mb-6 group-hover:bg-brand-500/20 transition-all">
                  <BarChart3 className="w-6 h-6" />
                </div>
                <h4 className="text-lg font-bold text-white mb-2">ATS & Match Analytics</h4>
                <p className="text-slate-400 text-xs leading-relaxed">
                  Evaluate your formatting compliance, check formatting defects, and compute exact semantic compatibility indexes against any target role description.
                </p>
              </div>

              <div className="glass p-8 rounded-3xl relative overflow-hidden group border-slate-800/60">
                <div className="p-3 bg-pink-500/10 rounded-2xl w-fit text-pink-400 mb-6 group-hover:bg-pink-500/20 transition-all">
                  <BookOpen className="w-6 h-6" />
                </div>
                <h4 className="text-lg font-bold text-white mb-2">Roadmap & Skill Gaps</h4>
                <p className="text-slate-400 text-xs leading-relaxed">
                  Extract missing requirements from target listings, rank missing technologies dynamically, and unlock structured daily agendas to fill core vacancies.
                </p>
              </div>

              <div className="glass p-8 rounded-3xl relative overflow-hidden group border-slate-800/60">
                <div className="p-3 bg-emerald-500/10 rounded-2xl w-fit text-emerald-400 mb-6 group-hover:bg-emerald-500/20 transition-all">
                  <MessageSquare className="w-6 h-6" />
                </div>
                <h4 className="text-lg font-bold text-white mb-2">Mock Packs & Twin Chat</h4>
                <p className="text-slate-400 text-xs leading-relaxed">
                  Unlock bespoke Technical, HR, and project questions. Engage with a contextual assistant that details adjustments based on your analysis context.
                </p>
              </div>
            </div>
          </header>

          {/* FOOTER */}
          <footer className="glass border-t border-slate-800/60 py-8 px-6 mt-auto">
            <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
              <span className="text-xs text-slate-500">© 2026 CareerPilot AI Employability Platform. Built with Spring Boot & Gemini.</span>
              <div className="flex gap-4 text-xs text-slate-400">
                <a href="#features" className="hover:text-white">Features</a>
                <a href="#workflow" className="hover:text-white">Workflow</a>
                <a href="#privacy" className="hover:text-white">Data Privacy</a>
              </div>
            </div>
          </footer>
        </div>
      )}

      {/* VIEW: LOADING */}
      {view === 'loading' && (
        <div className="flex-1 flex flex-col items-center justify-center p-6">
          <div className="relative w-24 h-24 mb-8">
            <div className="absolute inset-0 rounded-full border-4 border-slate-800"></div>
            <div className="absolute inset-0 rounded-full border-4 border-t-brand-500 border-l-pink-500 animate-spin"></div>
            <div className="absolute inset-2 bg-slate-950 rounded-full flex items-center justify-center">
              <Sparkles className="w-6 h-6 text-brand-400 animate-pulse" />
            </div>
          </div>
          <h3 className="text-lg font-bold text-white mb-2">Processing with CareerPilot AI</h3>
          <p className="text-xs text-slate-400 animate-pulse tracking-wide font-medium">{loadingMessage}</p>
        </div>
      )}

      {/* VIEW: DASHBOARD */}
      {view === 'dashboard' && (
        <div className="flex-1 flex flex-col">
          {/* Main layout grid - left is dashboard tabs, right is Assistant chat sidebar */}
          <div className="flex-1 grid grid-cols-1 lg:grid-cols-4 gap-6 p-6 max-w-[1600px] w-full mx-auto">
            
            {/* LEFT SIDEBAR NAVIGATION & MAIN MODULE CONTENT */}
            <div className="lg:col-span-3 flex flex-col space-y-6">
              
              {/* TOP METRICS ROW */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
                <CircularGauge score={analysis?.applicationReadinessScore || 70} title="Career Readiness Score" strokeColor="#3b82f6" />
                <CircularGauge score={analysis?.matchScore || 75} title="Job Match Alignment" strokeColor="#10b981" />
                <CircularGauge score={analysis?.atsScore || 65} title="ATS Compatibility" strokeColor="#ec4899" />
              </div>

              {/* TABS CONTAINER */}
              <div className="glass-card rounded-2xl overflow-hidden flex flex-col flex-1 border-slate-800">
                
                {/* TAB SELECTORS */}
                <div className="border-b border-slate-800/80 bg-slate-900/50 flex flex-wrap gap-1 p-2">
                  {[
                    { id: 'overview', label: 'Overview', icon: FileText },
                    { id: 'match', label: 'Match Details', icon: CheckCircle2 },
                    { id: 'ats', label: 'ATS Report', icon: ShieldCheck },
                    { id: 'skills', label: 'Skill Gap', icon: Layers },
                    { id: 'roadmap', label: 'Roadmap Plan', icon: BookOpen },
                    { id: 'interview', label: 'Prep Pack', icon: Award },
                    { id: 'twin', label: 'Twin Stats', icon: User },
                  ].map((tab) => {
                    const Icon = tab.icon;
                    const active = activeTab === tab.id;
                    return (
                      <button key={tab.id} onClick={() => setActiveTab(tab.id)}
                        className={`flex items-center gap-1.5 px-4 py-2.5 rounded-xl text-xs font-bold transition-all ${
                          active 
                            ? 'bg-brand-600 text-white shadow-md shadow-brand-500/10' 
                            : 'text-slate-400 hover:text-slate-200 hover:bg-slate-850'
                        }`}>
                        <Icon className="w-4 h-4" />
                        {tab.label}
                      </button>
                    );
                  })}
                </div>

                {/* TAB DETAILS CONTENT */}
                <div className="p-6 md:p-8 flex-1 overflow-y-auto max-h-[650px]">
                  
                  {/* TAB: OVERVIEW */}
                  {activeTab === 'overview' && (
                    <div className="space-y-6">
                      <div className="flex justify-between items-start border-b border-slate-800 pb-4">
                        <div>
                          <h2 className="text-2xl font-bold text-white">{resume?.name}</h2>
                          <p className="text-xs text-slate-400 flex items-center gap-3 mt-1">
                            <span>📧 {resume?.email}</span>
                            <span>📞 {resume?.phone}</span>
                          </p>
                        </div>
                        <span className="px-3 py-1 bg-brand-500/10 text-brand-400 border border-brand-500/20 rounded-full text-[10px] font-bold uppercase tracking-wider">
                          Ready for: {analysis?.careerLevelTarget}
                        </span>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
                        {/* Skills Overview */}
                        <div className="bg-slate-900/40 border border-slate-800/80 rounded-xl p-5">
                          <h4 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
                            <Layers className="w-4.5 h-4.5 text-brand-400" /> Extracted Skills
                          </h4>
                          <div className="flex flex-wrap gap-2">
                            {parseJsonArray(resume?.skills).map((skill, idx) => (
                              <span key={idx} className="px-2.5 py-1 bg-slate-800 text-slate-300 rounded-lg text-xs font-semibold">
                                {skill}
                              </span>
                            ))}
                          </div>
                        </div>

                        {/* Education Details */}
                        <div className="bg-slate-900/40 border border-slate-800/80 rounded-xl p-5">
                          <h4 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
                            <BookOpen className="w-4.5 h-4.5 text-pink-400" /> Education History
                          </h4>
                          <div className="space-y-3">
                            {parseJsonArray(resume?.education).map((edu, idx) => (
                              <div key={idx} className="text-xs">
                                <p className="font-semibold text-white">{edu.degree}{edu.branch ? ` in ${edu.branch}` : ''}</p>
                                <p className="text-slate-400 mt-0.5">{edu.university || edu.institution} | {edu.year || edu.duration}</p>
                              </div>
                            ))}
                          </div>
                        </div>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Projects */}
                        <div className="bg-slate-900/40 border border-slate-800/80 rounded-xl p-5">
                          <h4 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
                            <Terminal className="w-4.5 h-4.5 text-indigo-400" /> Projects Overview
                          </h4>
                          <div className="space-y-4">
                            {parseJsonArray(resume?.projects).map((proj, idx) => (
                              <div key={idx} className="text-xs">
                                <p className="font-semibold text-slate-200">{proj.title}</p>
                                <p className="text-slate-400 mt-1">{proj.description}</p>
                                <div className="flex flex-wrap gap-1.5 mt-2">
                                  {proj.technologies?.map((tech, tIdx) => (
                                    <span key={tIdx} className="px-2 py-0.5 bg-slate-800/60 border border-slate-700/40 text-[10px] text-slate-400 rounded">
                                      {tech}
                                    </span>
                                  ))}
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>

                        {/* Experience */}
                        <div className="bg-slate-900/40 border border-slate-800/80 rounded-xl p-5">
                          <h4 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
                            <Award className="w-4.5 h-4.5 text-emerald-400" /> Professional Experience
                          </h4>
                          <div className="space-y-4">
                            {parseJsonArray(resume?.experience).map((exp, idx) => (
                              <div key={idx} className="text-xs">
                                <p className="font-semibold text-slate-200">{exp.role} @ {exp.company}</p>
                                <p className="text-[10px] text-slate-500 font-semibold">{exp.duration}</p>
                                <p className="text-slate-400 mt-1.5 leading-relaxed">{exp.description}</p>
                              </div>
                            ))}
                            {parseJsonArray(resume?.experience).length === 0 && (
                              <p className="text-xs text-slate-500">No professional experience listed yet.</p>
                            )}
                          </div>
                        </div>
                      </div>

                    </div>
                  )}

                  {/* TAB: JOB MATCH DETAILS */}
                  {activeTab === 'match' && (
                    <div className="space-y-6">
                      <div className="border-b border-slate-800 pb-4">
                        <h2 className="text-lg font-bold text-white flex items-center gap-2">
                          <CheckCircle2 className="w-5 h-5 text-emerald-400" /> Role Alignment Analysis
                        </h2>
                        <p className="text-xs text-slate-400 mt-1">Audit of target parameters matches against your parsed competencies.</p>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Strengths */}
                        <div className="glass p-5 rounded-xl border-emerald-500/10">
                          <h4 className="text-xs font-bold uppercase tracking-wider text-emerald-400 mb-3 flex items-center gap-1.5">
                            <CheckCircle2 className="w-4 h-4" /> Core Strengths
                          </h4>
                          <ul className="space-y-2.5 text-xs text-slate-300">
                            {parseJsonArray(analysis?.strengths).map((str, idx) => (
                              <li key={idx} className="flex items-start gap-2 leading-relaxed">
                                <span className="text-emerald-400 font-bold mt-0.5">✓</span>
                                <span>{str}</span>
                              </li>
                            ))}
                          </ul>
                        </div>

                        {/* Weaknesses */}
                        <div className="glass p-5 rounded-xl border-pink-500/10">
                          <h4 className="text-xs font-bold uppercase tracking-wider text-pink-400 mb-3 flex items-center gap-1.5">
                            <AlertCircle className="w-4 h-4" /> Competency Deficits
                          </h4>
                          <ul className="space-y-2.5 text-xs text-slate-300">
                            {parseJsonArray(analysis?.weaknesses).map((wk, idx) => (
                              <li key={idx} className="flex items-start gap-2 leading-relaxed">
                                <span className="text-pink-400 font-bold mt-0.5">✕</span>
                                <span>{wk}</span>
                              </li>
                            ))}
                          </ul>
                        </div>
                      </div>

                      {/* Skill Comparisons */}
                      <div className="bg-slate-900/40 border border-slate-800/80 rounded-xl p-5">
                        <h4 className="text-sm font-bold text-white mb-4">Competency Mapping Index</h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                          <div>
                            <p className="text-xs font-bold text-emerald-400 mb-2 uppercase tracking-wide">Matched Capabilities</p>
                            <div className="flex flex-wrap gap-1.5">
                              {parseJsonArray(analysis?.matchedSkills).map((s, idx) => (
                                <span key={idx} className="px-2 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded text-xs">
                                  {s}
                                </span>
                              ))}
                            </div>
                          </div>
                          <div>
                            <p className="text-xs font-bold text-pink-400 mb-2 uppercase tracking-wide">Missing Core Requirements</p>
                            <div className="flex flex-wrap gap-1.5">
                              {parseJsonArray(analysis?.missingSkills).map((s, idx) => (
                                <span key={idx} className="px-2 py-1 bg-pink-500/10 border border-pink-500/20 text-pink-400 rounded text-xs">
                                  {s}
                                </span>
                              ))}
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* TAB: ATS REPORT */}
                  {activeTab === 'ats' && (
                    <div className="space-y-6">
                      <div className="border-b border-slate-800 pb-4">
                        <h2 className="text-lg font-bold text-white flex items-center gap-2">
                          <ShieldCheck className="w-5 h-5 text-pink-500" /> ATS Compatibility & Optimization
                        </h2>
                        <p className="text-xs text-slate-400 mt-1">Recommendations to bypass Application Tracking Systems algorithms.</p>
                      </div>

                      {/* Missing Keywords Heatmap visual */}
                      <div className="bg-slate-900/50 border border-slate-800/80 rounded-xl p-5">
                        <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">Critical Missing Keywords</h4>
                        <div className="flex flex-wrap gap-2">
                          {parseJsonArray(analysis?.missingKeywords).map((kw, idx) => (
                            <span key={idx} className="px-3 py-1.5 bg-slate-950 border border-rose-500/30 text-rose-300 rounded-lg text-xs font-bold relative group">
                              <span className="absolute -top-1 -right-1 w-2.5 h-2.5 bg-rose-500 rounded-full animate-pulse"></span>
                              {kw}
                            </span>
                          ))}
                          {parseJsonArray(analysis?.missingKeywords).length === 0 && (
                            <span className="text-xs text-emerald-400">All matching keywords are covered in your CV!</span>
                          )}
                        </div>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Improvements list */}
                        <div className="glass p-5 rounded-xl border-slate-800">
                          <h4 className="text-xs font-bold uppercase tracking-wider text-brand-300 mb-3">Content Recommendations</h4>
                          <ul className="space-y-3 text-xs text-slate-300 list-disc list-inside leading-relaxed">
                            {parseJsonArray(analysis?.resumeImprovements).map((imp, idx) => (
                              <li key={idx} className="pl-1 text-slate-300">{imp}</li>
                            ))}
                          </ul>
                        </div>

                        {/* Format improvements list */}
                        <div className="glass p-5 rounded-xl border-slate-800">
                          <h4 className="text-xs font-bold uppercase tracking-wider text-brand-300 mb-3">Formatting Compliance</h4>
                          <ul className="space-y-3 text-xs text-slate-300 list-disc list-inside leading-relaxed">
                            {parseJsonArray(analysis?.atsRecommendations).map((rec, idx) => (
                              <li key={idx} className="pl-1 text-slate-300">{rec}</li>
                            ))}
                          </ul>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* TAB: SKILL GAP ANALYSIS */}
                  {activeTab === 'skills' && (
                    <div className="space-y-6">
                      <div className="border-b border-slate-800 pb-4">
                        <h2 className="text-lg font-bold text-white flex items-center gap-2">
                          <Layers className="w-5 h-5 text-indigo-400" /> Technology Gap Diagnosis
                        </h2>
                        <p className="text-xs text-slate-400 mt-1">Detailed inventory of technologies required vs. technologies present.</p>
                      </div>

                      {/* Rank priority table */}
                      <div className="bg-slate-900/50 border border-slate-800/80 rounded-xl overflow-hidden">
                        <div className="grid grid-cols-3 gap-4 bg-slate-800/50 px-5 py-3 text-[10px] font-bold uppercase tracking-wider text-slate-400 border-b border-slate-800">
                          <span>Technology</span>
                          <span>Priority Status</span>
                          <span>Avg. Prep Time</span>
                        </div>
                        <div className="divide-y divide-slate-800/80">
                          {parseJsonArray(analysis?.skillPriorityRanking).map((item, idx) => (
                            <div key={idx} className="grid grid-cols-3 gap-4 px-5 py-3 text-xs items-center">
                              <span className="font-bold text-slate-200">{item.skill}</span>
                              <div>
                                <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                                  item.priority === 'High' ? 'bg-rose-500/15 text-rose-400 border border-rose-500/20' :
                                  item.priority === 'Medium' ? 'bg-amber-500/15 text-amber-400 border border-amber-500/20' :
                                  'bg-slate-800 text-slate-400 border border-slate-700/40'
                                }`}>
                                  {item.priority}
                                </span>
                              </div>
                              <span className="text-slate-400 flex items-center gap-1"><Clock className="w-3.5 h-3.5" /> {item.timeToLearn}</span>
                            </div>
                          ))}
                        </div>
                      </div>

                      {/* Why it matters note */}
                      <div className="glass p-5 rounded-xl border-brand-500/20 flex gap-4">
                        <AlertCircle className="w-6 h-6 text-brand-400 shrink-0 mt-0.5" />
                        <div>
                          <h4 className="text-xs font-bold uppercase tracking-wider text-brand-300 mb-1">Hiring Context & Impact</h4>
                          <p className="text-xs text-slate-300 leading-relaxed">{analysis?.explanationOfImportance}</p>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* TAB: PERSONALIZED ROADMAP */}
                  {activeTab === 'roadmap' && (
                    <div className="space-y-6">
                      <div className="border-b border-slate-800 pb-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2">
                        <div>
                          <h2 className="text-lg font-bold text-white flex items-center gap-2">
                            <BookOpen className="w-5 h-5 text-brand-400" /> Personalized Learning Roadmap
                          </h2>
                          <p className="text-xs text-slate-400 mt-1">Accelerated plan to close missing competency areas.</p>
                        </div>
                        <span className="px-3 py-1 bg-brand-500/15 text-brand-400 border border-brand-500/30 rounded-full text-xs font-bold">
                          Readiness Goal: {roadmap?.readinessTimeline || '30 Days'}
                        </span>
                      </div>

                      {/* Weekly milestones checklist layout */}
                      <div className="space-y-6 pt-2">
                        {parseJsonArray(roadmap?.weeklyMilestones).map((week, idx) => (
                          <div key={idx} className="glass rounded-xl p-5 relative overflow-hidden group border-slate-800/80">
                            <div className="absolute top-0 left-0 bottom-0 w-1 bg-brand-500"></div>
                            <div className="flex flex-col sm:flex-row justify-between items-start gap-4">
                              <div>
                                <span className="text-[10px] font-bold text-brand-400 uppercase tracking-widest">Week {week.week} Goal</span>
                                <h4 className="text-sm font-bold text-white mt-1">{week.goal}</h4>
                                <p className="text-xs text-slate-400 mt-2 leading-relaxed flex items-center gap-1.5">
                                  <CheckSquare className="w-4.5 h-4.5 text-brand-400 shrink-0" />
                                  <span><strong className="text-slate-300">Deliverable:</strong> {week.deliverable}</span>
                                </p>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>

                      {/* Detailed Daily agenda */}
                      <div className="bg-slate-900/50 border border-slate-800/80 rounded-xl p-5">
                        <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4">Milestone-by-Milestone Schedule</h4>
                        <div className="space-y-4 relative border-l border-slate-800 pl-4 ml-2">
                          {parseJsonArray(roadmap?.learningPlan30Days).map((day, idx) => (
                            <div key={idx} className="relative text-xs">
                              <span className="absolute -left-6.5 top-1.5 w-2.5 h-2.5 rounded-full bg-brand-500 ring-4 ring-slate-950"></span>
                              <p className="font-bold text-brand-300">Day {day.day} • {day.topic}</p>
                              <p className="text-slate-400 mt-1 leading-relaxed">{day.details}</p>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  )}

                  {/* TAB: INTERVIEW PREPARATION PACK */}
                  {activeTab === 'interview' && (
                    <div className="space-y-6">
                      <div className="border-b border-slate-800 pb-4">
                        <h2 className="text-lg font-bold text-white flex items-center gap-2">
                          <Award className="w-5 h-5 text-emerald-400" /> Customized Interview Prep Pack
                        </h2>
                        <p className="text-xs text-slate-400 mt-1">Generated question sets and answers reflecting your background projects.</p>
                      </div>

                      {/* Sub-sections: Tech, HR, Projects */}
                      <div className="space-y-6 pt-2">
                        {/* Technical */}
                        <div>
                          <h4 className="text-xs font-bold uppercase tracking-wider text-brand-400 mb-3 flex items-center gap-2">
                            <Terminal className="w-4 h-4" /> Technical & System Design Questions
                          </h4>
                          <div className="space-y-3">
                            {parseJsonArray(interviewPack?.technicalQuestions).map((q, idx) => {
                              const key = `tech-${idx}`;
                              const isVisible = visibleAnswers[key];
                              return (
                                <div key={idx} className="glass rounded-xl p-4 border-slate-800">
                                  <div className="flex justify-between items-start gap-4">
                                    <p className="text-xs font-bold text-white">Q: {q.question}</p>
                                    <button onClick={() => toggleAnswer(key)} className="text-brand-400 hover:text-brand-300 text-xs shrink-0 flex items-center gap-1 font-bold">
                                      {isVisible ? <><EyeOff className="w-3.5 h-3.5" /> Hide</> : <><Eye className="w-3.5 h-3.5" /> Answer</>}
                                    </button>
                                  </div>
                                  {isVisible && (
                                    <div className="mt-3 pt-3 border-t border-slate-800/80 text-xs text-slate-300 leading-relaxed bg-slate-900/40 p-3 rounded-lg border border-slate-800/60">
                                      {q.answer}
                                    </div>
                                  )}
                                </div>
                              );
                            })}
                          </div>
                        </div>

                        {/* Project questions */}
                        <div>
                          <h4 className="text-xs font-bold uppercase tracking-wider text-indigo-400 mb-3 flex items-center gap-2">
                            <Layers className="w-4 h-4" /> Project Architecture Deep-Dives
                          </h4>
                          <div className="space-y-3">
                            {parseJsonArray(interviewPack?.projectQuestions).map((q, idx) => {
                              const key = `proj-${idx}`;
                              const isVisible = visibleAnswers[key];
                              return (
                                <div key={idx} className="glass rounded-xl p-4 border-slate-800">
                                  <div className="flex justify-between items-start gap-4">
                                    <p className="text-xs font-bold text-white">Q: {q.question}</p>
                                    <button onClick={() => toggleAnswer(key)} className="text-indigo-400 hover:text-indigo-300 text-xs shrink-0 flex items-center gap-1 font-bold">
                                      {isVisible ? <><EyeOff className="w-3.5 h-3.5" /> Hide</> : <><Eye className="w-3.5 h-3.5" /> Answer</>}
                                    </button>
                                  </div>
                                  {isVisible && (
                                    <div className="mt-3 pt-3 border-t border-slate-800/80 text-xs text-slate-300 leading-relaxed bg-slate-900/40 p-3 rounded-lg border border-slate-800/60">
                                      {q.answer}
                                    </div>
                                  )}
                                </div>
                              );
                            })}
                          </div>
                        </div>

                        {/* HR / Behavioral */}
                        <div>
                          <h4 className="text-xs font-bold uppercase tracking-wider text-pink-400 mb-3 flex items-center gap-2">
                            <User className="w-4 h-4" /> Behavioral / HR Scenarios
                          </h4>
                          <div className="space-y-3">
                            {parseJsonArray(interviewPack?.hrQuestions).map((q, idx) => {
                              const key = `hr-${idx}`;
                              const isVisible = visibleAnswers[key];
                              return (
                                <div key={idx} className="glass rounded-xl p-4 border-slate-800">
                                  <div className="flex justify-between items-start gap-4">
                                    <p className="text-xs font-bold text-white">Q: {q.question}</p>
                                    <button onClick={() => toggleAnswer(key)} className="text-pink-400 hover:text-pink-300 text-xs shrink-0 flex items-center gap-1 font-bold">
                                      {isVisible ? <><EyeOff className="w-3.5 h-3.5" /> Hide</> : <><Eye className="w-3.5 h-3.5" /> Answer</>}
                                    </button>
                                  </div>
                                  {isVisible && (
                                    <div className="mt-3 pt-3 border-t border-slate-800/80 text-xs text-slate-300 leading-relaxed bg-slate-900/40 p-3 rounded-lg border border-slate-800/60">
                                      {q.answer}
                                    </div>
                                  )}
                                </div>
                              );
                            })}
                          </div>
                        </div>

                      </div>
                    </div>
                  )}

                  {/* TAB: CAREER TWIN DETAILS */}
                  {activeTab === 'twin' && (
                    <div className="space-y-6">
                      <div className="border-b border-slate-800 pb-4">
                        <h2 className="text-lg font-bold text-white flex items-center gap-2">
                          <User className="w-5 h-5 text-brand-400" /> Career Twin Analysis
                        </h2>
                        <p className="text-xs text-slate-400 mt-1">Projection mapping current academic experience level to junior software industry target roles.</p>
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 pt-2">
                        {/* Current vs Target mapping cards */}
                        <div className="glass p-5 rounded-2xl relative overflow-hidden flex flex-col justify-center min-h-[140px]">
                          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Diagnosed Level</span>
                          <span className="text-2xl font-extrabold text-white mt-1">{analysis?.careerLevelCurrent}</span>
                          <div className="w-full bg-slate-800 h-1.5 rounded-full mt-4 overflow-hidden">
                            <div className="bg-brand-500 h-full w-[45%]"></div>
                          </div>
                        </div>

                        <div className="glass p-5 rounded-2xl relative overflow-hidden flex flex-col justify-center min-h-[140px] border-brand-500/20">
                          <div className="absolute top-2 right-2 p-1.5 bg-brand-500/10 rounded-lg text-brand-400">
                            <Clock className="w-4.5 h-4.5" />
                          </div>
                          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Target Role Benchmark</span>
                          <span className="text-2xl font-extrabold text-brand-300 mt-1">{analysis?.careerLevelTarget}</span>
                          <p className="text-[10px] text-slate-400 mt-3 uppercase tracking-wider font-semibold">Preparation delta: {analysis?.careerTimeToReach}</p>
                        </div>
                      </div>

                      {/* Missing competencies list */}
                      <div className="bg-slate-900/50 border border-slate-800/80 rounded-xl p-5">
                        <h4 className="text-sm font-bold text-white mb-3">Target Competency Delays</h4>
                        <div className="space-y-3">
                          {parseJsonArray(analysis?.careerMissingCompetencies).map((comp, idx) => (
                            <div key={idx} className="flex items-center gap-3 text-xs text-slate-300">
                              <span className="w-2.5 h-2.5 rounded-full bg-rose-500 shadow-md shadow-rose-500/50 shrink-0"></span>
                              <span>{comp}</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  )}

                </div>

              </div>

            </div>

            {/* RIGHT COLUMN: AI CAREER TWIN ASSISTANT CHAT PANEL */}
            <div className="lg:col-span-1 flex flex-col glass-premium rounded-2xl border-slate-800 h-[750px] relative overflow-hidden">
              {/* Header */}
              <div className="p-4 border-b border-slate-800 bg-slate-900/60 flex items-center gap-2">
                <div className="p-1.5 bg-brand-500/10 text-brand-400 rounded-lg">
                  <Sparkles className="w-4 h-4 animate-spin-slow" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-white uppercase tracking-wider">Twin Assistant</h4>
                  <p className="text-[9px] text-emerald-400 uppercase tracking-widest -mt-0.5 font-bold">Context Active</p>
                </div>
              </div>

              {/* Message History list */}
              <div className="flex-1 overflow-y-auto p-4 space-y-4 scroll-smooth">
                {chatHistory.map((chat, idx) => {
                  const isUser = chat.sender === 'USER';
                  return (
                    <div key={idx} className={`flex flex-col ${isUser ? 'items-end' : 'items-start'}`}>
                      <span className="text-[9px] text-slate-500 uppercase tracking-widest font-bold mb-1 px-1">
                        {isUser ? 'You' : 'CareerPilot AI'}
                      </span>
                      <div className={`text-xs rounded-2xl px-3.5 py-2.5 max-w-[90%] leading-relaxed ${
                        isUser 
                          ? 'bg-brand-600 text-white rounded-tr-none' 
                          : 'bg-slate-900 text-slate-300 border border-slate-800/80 rounded-tl-none'
                      }`}>
                        {chat.message}
                      </div>
                    </div>
                  );
                })}

                {/* AI Typings Loader */}
                {chatLoading && (
                  <div className="flex flex-col items-start">
                    <span className="text-[9px] text-slate-500 uppercase tracking-widest font-bold mb-1 px-1">CareerPilot AI</span>
                    <div className="bg-slate-900 text-slate-400 border border-slate-850 rounded-2xl rounded-tl-none px-3.5 py-3 flex gap-1 items-center">
                      <span className="w-1.5 h-1.5 bg-brand-400 rounded-full animate-bounce"></span>
                      <span className="w-1.5 h-1.5 bg-brand-400 rounded-full animate-bounce delay-100"></span>
                      <span className="w-1.5 h-1.5 bg-brand-400 rounded-full animate-bounce delay-200"></span>
                    </div>
                  </div>
                )}
                
                <div ref={chatEndRef}></div>
              </div>

              {/* Chat Quick Queries buttons */}
              <div className="px-4 py-2 border-t border-slate-800/80 flex flex-wrap gap-1 bg-slate-900/30">
                {[
                  { text: 'Why is my score low?', val: 'Why is my readiness score low, and which skills are penalizing it most?' },
                  { text: 'Improve ATS?', val: 'What are the top three specific content revisions I should make to improve my ATS score?' },
                  { text: 'What to learn next?', val: 'According to my priority ranking, what technology should I start learning today?' },
                ].map((q, idx) => (
                  <button key={idx} onClick={() => setChatInput(q.val)} className="text-[9px] text-brand-300 hover:text-white glass px-2 py-1 rounded-lg transition-all hover:bg-brand-600/10 truncate max-w-full">
                    {q.text}
                  </button>
                ))}
              </div>

              {/* Chat Input form */}
              <form onSubmit={handleSendChatMessage} className="p-3 border-t border-slate-800 bg-slate-900/80 flex gap-2">
                <input type="text" placeholder="Ask your Twin Assistant..." value={chatInput} onChange={(e) => setChatInput(e.target.value)}
                  className="flex-1 bg-slate-950 border border-slate-850 rounded-xl px-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500" />
                <button type="submit" disabled={chatLoading} className="p-2.5 bg-brand-600 text-white rounded-xl hover:bg-brand-500 disabled:opacity-50 transition-all flex items-center justify-center">
                  <Send className="w-4 h-4" />
                </button>
              </form>
            </div>

          </div>
        </div>
      )}
    </>
  );
}
