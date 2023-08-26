import './App.scss';
import {HashRouter as Router, Routes ,Route} from 'react-router-dom';
import { HomePage } from './pages/HomePage';

export function App() {


  return (
    <div className="App">
      <Router>
        <Routes>
         <Route path='/' element={<HomePage/>} />

        </Routes>
      </Router>
    </div>
  );
}

export default App;
