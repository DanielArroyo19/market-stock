import { React, useEffect, useState } from 'react';
import { DataGrid } from '@mui/x-data-grid';
import axios from 'axios';

export function StockList() {

const columns = [
  { field: 'id', headerName: 'ID', width: 90 },
  {
    field: 'name',
    headerName: 'Stock Name',
    width: 150,
    editable: true,
  }
];


const [stocks, setStocks] = useState([]);
  useEffect(
      () => {
       const fetchAllStocks = async () => {
          await axios.get(process.env.REACT_APP_API_ROOT_URL + '/v1/symbol/')
          .then(res => {
            const stocks = res.data;
            console.log(stocks);
            setStocks(stocks);
          }).catch(error => {
              console.log('Error here' + error);
            });
       };
       fetchAllStocks();
      }, []
  );
const rows = stocks.map((stock)=> {
          console.log(stock);
          return {
                  id:stock.id,
                  name:stock.name,
                }
              });





     return (
        <div style={{ height: 400, width: '100%' }}>
          <DataGrid
            rows={rows}
            columns={columns}
            pageSize={5}
            rowsPerPageOptions={[5]}
          />
        </div>
      );
}
