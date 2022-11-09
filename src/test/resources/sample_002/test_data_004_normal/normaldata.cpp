#include <iostream>

using namespace std;

char char1 = 'c';

int functionB(int x) {
  return x*x;
}

int functionA(int x, int y) {
  int xx = functionB(x);
  int yy = functionB(y);

  return xx + yy;
}


int main(int argc, char* argv[]) {

  cout << functionA(10, 10) << endl;
	char1 = 'z';

	return 0;
}

