import { React, useEffect, useState } from 'react';
import './HomePage.scss';
import { StockList } from '../components/StockList';




export const HomePage = () => {

    return (
        <div className="HomePage">
            <div className="header-section">
                <h1 className="app-name">Stock Market Panel</h1>
            </div>
            <div>
                <StockList />
            </div>
        </div>
        
    );
}