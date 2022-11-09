#include <iostream>
 
using namespace std;

class Hinh 
{
protected:
  int MAX_CHIEURONG = 300;
  int MAX_CHIEUCAO = 300;
};

class HinhChuNhat: public Hinh
{
public:
  int chieurong;
  int chieucao;

  void setChieuRong(int rong)
  {
    chieurong = rong;
  }

  void setChieuCao(int cao)
  {
    chieucao = cao;
  }
  
  HinhChuNhat() {};

  HinhChuNhat(int rong) {
    this->chieurong = rong;
  }

  HinhChuNhat(int rong, int cao) {
    this->chieurong = rong;
    this->chieucao = cao;
  }
    
  int tinhDienTich()
  { 
    return chieurong * chieucao; 
  }
};

HinhChuNhat changeRongHCN(HinhChuNhat hcn, int rong) {
  hcn.setChieuRong(rong);
  return hcn;
}

HinhChuNhat changeCaoHCN(HinhChuNhat hcn, int cao) {
  hcn.setChieuCao(cao);
  return hcn;
}

HinhChuNhat changeKichThuocHCN(HinhChuNhat hcn, int rong, int cao) {
  HinhChuNhat tmp = changeRongHCN(hcn, rong);
  tmp = changeRongHCN(hcn, cao);
  return tmp;
}

int main(void)
{
  HinhChuNhat Hcn;
 
  Hcn.setChieuRong(14);
  Hcn.setChieuCao(30);

  cout << "Tong dien tich la: " << Hcn.tinhDienTich() << endl;

  HinhChuNhat newHCN = changeKichThuocHCN(Hcn, 20, 30);

  cout << "Tong dien tich la: " << newHCN.tinhDienTich() << endl;
  return 0;
}
