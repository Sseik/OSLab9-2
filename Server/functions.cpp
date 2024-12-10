#include "functions.h"
#include <fileapi.h>

#define MAX_TIME 60

map<string, DirectoryInfo> cache;

DirectoryInfo FindInformation(string& directory) {
	time_t now = time(NULL);
	// Повертаємо інформацію з кешу, якщо попередньому запису не більше MAX_TIME секунд
	/*if (cache.count(directory) && now - cache[directory].searchTime <= MAX_TIME)
		return cache[directory];*/
	DirectoryInfo res = {};
	WIN32_FIND_DATAA fileInfo = {};
	HANDLE h = FindFirstFileA((directory + "/*.*").c_str(), &fileInfo);
	if (h != INVALID_HANDLE_VALUE) {
		do {
			string name = fileInfo.cFileName;
			if (name == "." || name == "..")
				continue;
			res.searchTime = now;
			res.quantity++;
			res.size += fileInfo.nFileSizeLow + (unsigned long long) fileInfo.nFileSizeHigh << 32;
			string type = "folder";
			int pos = name.rfind('.');
			if (pos != string::npos)
				type = name.substr(pos);
			//std::copy(name.begin() + name.rfind('.') + 1, name.end(), type.begin());
			const FileInfo fi = { name, type, fileInfo.ftCreationTime };
			res.files.push_back(fi);
		} while (FindNextFileA(h, &fileInfo));
	}
	return res;
}
