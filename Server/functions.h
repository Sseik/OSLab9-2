#pragma once
#include <string>
#include <map>
#include <vector>
#include <Windows.h>
#include <time.h>

using std::string;
using std::map;
using std::vector;

typedef struct SFileInfo {
	string name;
	string type;
	FILETIME creationTime;
} FileInfo;

typedef struct SDirectoryInfo {
	int quantity;
	time_t searchTime;
	double size;
	vector<FileInfo> files;

} DirectoryInfo;

DirectoryInfo FindInformation(string& directory);
