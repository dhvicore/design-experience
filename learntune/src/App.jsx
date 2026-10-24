import { useState } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Topics from "./pages/Topics";
import LearningPage from "./pages/LearningPage";
import SongLibrary from "./pages/SongLibrary";
import "./App.css";

export default function App() {
  const [language, setLanguage] = useState("en");

  return (
    <BrowserRouter>
      <div className="app">
        <Navbar language={language} setLanguage={setLanguage} />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/topics" element={<Topics language={language} />} />
            <Route
              path="/learn/:topicId"
              element={<LearningPage language={language} setLanguage={setLanguage} />}
            />
            <Route path="/songs" element={<SongLibrary />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}
