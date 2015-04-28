/**
 * This file is part of XY.Codebase, Copyright 2011 (C) Xyan Kruse, Xyan@gmx.net, Xyan.kilu.de
 * 
 * XY.Codebase is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * XY.Codebase is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with XY.Codebase. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.xy.codebasel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class File {
    /**
     * buffersize for copy operations
     */
    public static final int COPY_BUFFER = 4 * 1024; // 4kb

    /**
     * appends an file to out
     * 
     * @param target
     * @param out
     * @throws IOException
     */
    public static void getFile(final java.io.File target, final OutputStream out)
            throws IOException {
        if (target.isFile()) {
            final FileInputStream in = new FileInputStream(target);
            final byte[] buffer = new byte[COPY_BUFFER];
            while (-1 != in.read(buffer)) {
                out.write(buffer);
            }
        } else {
            throw new FileNotFoundException(Debug.values("Target is no file",
                    new Object[] { target }));
        }
    }
}
