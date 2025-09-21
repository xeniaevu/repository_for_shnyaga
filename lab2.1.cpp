#include <iostream>
#include <string>
using namespace std;

class InternetOperator {
private:
    static long long tariffCost;   
    string operatorName;            
    static long long totalRevenueAll; 
    long long subscribersCount;    

public:
   
    InternetOperator(string name, long long count) {
        operatorName = name;
        subscribersCount = count;
        totalRevenueAll += subscribersCount * tariffCost;
    }

   
    static void setTariffCost(long long cost) {
        tariffCost = cost;
    }

    
    static long long getTariffCost() {
        return tariffCost;
    }

   
    long long calculateRevenue() const {
        return subscribersCount * tariffCost;
    }

    
    void printInfo() const {
        cout << "Оператор: " << operatorName
             << ", абонентов: " << subscribersCount
             << ", стоимость тарифа: " << tariffCost << " руб." << endl;
    }

    static long long getTotalRevenueAll() {
        return totalRevenueAll;
    }
};

long long InternetOperator::tariffCost = 0;
long long InternetOperator::totalRevenueAll = 0;

int main() {
   
    InternetOperator::setTariffCost(500);

    
    InternetOperator megafon("Мегафон", 5);
    megafon.printInfo();
    cout << "Общая выручка: " << megafon.calculateRevenue() << " руб." << endl;

    InternetOperator mts("Мтс", 4);
    mts.printInfo();
     cout << "Общая выручка: " << mts.calculateRevenue() << " руб." << endl;

    InternetOperator tele2("Теле 2", 3);
    tele2.printInfo();
    cout << "Общая выручка: " << tele2.calculateRevenue() << " руб." << endl;



        cout << "Общая выручка всех операторов: " 
         << InternetOperator::getTotalRevenueAll()
         << " руб." << endl;
        
}